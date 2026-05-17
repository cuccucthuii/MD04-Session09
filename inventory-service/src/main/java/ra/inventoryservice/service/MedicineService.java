package ra.inventoryservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ra.inventoryservice.entity.Medicine;
import ra.inventoryservice.event.PharmacyAlert;
import ra.inventoryservice.pubsub.PharmacyAlertPublisher;
import ra.inventoryservice.repository.MedicineRepository;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicineService {

    private static final long LOCK_WAIT_SECONDS = 3;
    private static final long LOCK_LEASE_SECONDS = 5;

    private final MedicineRepository medicineRepository;
    private final PharmacyAlertPublisher pharmacyAlertPublisher;
    private final RedissonClient redissonClient;
    private final MedicineStockService medicineStockService;

    /**
     * Cache-aside: lần đầu truy vấn DB và lưu Redis; lần sau trả từ cache, không gọi method body.
     */
    @Cacheable(value = "medicines", key = "#id")
    public Medicine getMedicineById(Long id) {
        log.info("[medicine-cache] Cache MISS — truy vấn database cho thuốc id={}", id);
        return medicineRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy thuốc id=" + id));
    }

    /**
     * Cập nhật giá thuốc và xóa cache ngay — lần GET sau sẽ nạp lại từ DB.
     */
    @Transactional
    @CacheEvict(value = "medicines", key = "#medicine.id")
    public Medicine updateMedicine(Medicine medicine) {
        Medicine existing = medicineRepository.findById(medicine.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy thuốc id=" + medicine.getId()));

        if (medicine.getPrice() != null) {
            existing.setPrice(medicine.getPrice());
        }

        Medicine saved = medicineRepository.save(existing);
        log.info("[medicine-cache] @CacheEvict — đã xóa cache medicines::{} (giá mới={})",
                saved.getId(), saved.getPrice());
        return saved;
    }

    /**
     * Nhập thêm hàng — cập nhật DB và publish alert lên Redis channel (fire-and-forget).
     */
    @Transactional
    @CacheEvict(value = "medicines", key = "#id")
    public Medicine importStock(Long id, int quantity) {
        if (quantity <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Số lượng nhập phải > 0");
        }

        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy thuốc id=" + id));

        medicine.setStock(medicine.getStock() + quantity);
        Medicine saved = medicineRepository.save(medicine);

        PharmacyAlert alert = PharmacyAlert.builder()
                .type("IMPORT")
                .message(String.format("Đã nhập %d hộp %s", quantity, saved.getName()))
                .build();

        pharmacyAlertPublisher.publish(alert);
        log.info("[import-stock] id={}, quantity={}, tồn mới={}", id, quantity, saved.getStock());
        return saved;
    }

    /**
     * Bán 1 đơn vị — dùng Redisson RLock chống race condition khi nhiều nhân viên bán cùng lúc.
     */
    public String sellMedicine(Long id) {
        String lockKey = "lock:medicine:" + id;
        RLock lock = redissonClient.getLock(lockKey);
        boolean acquired = false;

        try {
            acquired = lock.tryLock(LOCK_WAIT_SECONDS, LOCK_LEASE_SECONDS, TimeUnit.SECONDS);
            if (!acquired) {
                log.warn("[distributed-lock] Không lấy được lock {} trong {}s", lockKey, LOCK_WAIT_SECONDS);
                return "Không thể xử lý thanh toán, vui lòng thử lại sau";
            }

            log.info("[distributed-lock] Đã lấy lock {}", lockKey);
            return medicineStockService.deductOneUnit(id);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "Giao dịch bị gián đoạn";
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("[distributed-lock] Đã giải phóng lock {}", lockKey);
            }
        }
    }

}
