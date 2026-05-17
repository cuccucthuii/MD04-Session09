package ra.inventoryservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ra.inventoryservice.entity.Medicine;
import ra.inventoryservice.repository.MedicineRepository;

@Service
@RequiredArgsConstructor
public class MedicineStockService {

    private final MedicineRepository medicineRepository;

    @Transactional
    @CacheEvict(value = "medicines", key = "#id")
    public String deductOneUnit(Long id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy thuốc id=" + id));

        if (medicine.getStock() <= 0) {
            return "Sản phẩm đã hết hàng!";
        }

        medicine.setStock(medicine.getStock() - 1);
        medicineRepository.save(medicine);
        return "Thanh toán thành công thuốc: " + medicine.getName();
    }
}
