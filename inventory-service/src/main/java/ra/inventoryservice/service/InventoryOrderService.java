package ra.inventoryservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ra.inventoryservice.entity.Medicine;
import ra.inventoryservice.entity.SalesOrder;
import ra.inventoryservice.event.OrderEvent;
import ra.inventoryservice.repository.MedicineRepository;
import ra.inventoryservice.repository.SalesOrderRepository;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryOrderService {

    private final MedicineRepository medicineRepository;
    private final SalesOrderRepository salesOrderRepository;

    /**
     * Idempotent theo kafka_order_id; trừ tồn trong transaction (khóa pessimistic).
     */
    @Transactional
    public void processOrderEvent(OrderEvent event) {
        if (event.getOrderId() == null || event.getMedicineId() == null || event.getQuantity() == null) {
            log.warn("[inventory] Bỏ qua event thiếu dữ liệu: {}", event);
            return;
        }

        if (salesOrderRepository.existsByKafkaOrderId(event.getOrderId())) {
            log.warn("[inventory] Đơn orderId={} đã xử lý — bỏ qua (consumer group không xử lý trùng).",
                    event.getOrderId());
            return;
        }

        Medicine medicine = medicineRepository.findByIdForUpdate(event.getMedicineId())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy thuốc id=" + event.getMedicineId()));

        if (medicine.getStock() < event.getQuantity()) {
            log.error("[inventory] Không đủ tồn kho medicineId={}, cần {}, hiện có {}",
                    event.getMedicineId(), event.getQuantity(), medicine.getStock());
            throw new IllegalStateException("Insufficient stock");
        }

        medicine.setStock(medicine.getStock() - event.getQuantity());
        medicineRepository.save(medicine);

        Instant receivedAt = event.getTimestamp() != null ? event.getTimestamp() : Instant.now();
        salesOrderRepository.save(SalesOrder.builder()
                .kafkaOrderId(event.getOrderId())
                .medicineId(event.getMedicineId())
                .quantity(event.getQuantity())
                .receivedAt(receivedAt)
                .build());

        log.info("[inventory] Đã nhận sự kiện đơn hàng — orderId={}, medicineId={}, quantity={} | " +
                        "Đã trừ kho và lưu đơn hàng. Tồn mới = {}",
                event.getOrderId(), event.getMedicineId(), event.getQuantity(), medicine.getStock());
    }
}
