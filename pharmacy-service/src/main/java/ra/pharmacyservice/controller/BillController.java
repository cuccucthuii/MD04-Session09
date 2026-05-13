package ra.pharmacyservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ra.pharmacyservice.dto.BillRequest;
import ra.pharmacyservice.dto.SellMedicineRequest;
import ra.pharmacyservice.event.OrderEvent;
import ra.pharmacyservice.service.ElectronicInvoiceService;
import ra.pharmacyservice.service.OrderEventProducer;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RefreshScope
@RequestMapping("/api/v1/bills")
@RequiredArgsConstructor
public class BillController {
    private final ElectronicInvoiceService electronicInvoiceService;
    private final OrderEventProducer orderEventProducer;

    @Value("${pharmacy.vat-rate}")
    private Double vatRate;

    @PostMapping("/e-invoice")
    public Map<String, Object> exportElectronicInvoice(@RequestBody BillRequest request) {
        return electronicInvoiceService.exportElectronicInvoice(request);
    }

    /**
     * Thanh toán / bán thuốc: gửi {@link OrderEvent} lên Kafka (topic medicine-stock-events),
     * key = medicineId để phân partition theo loại thuốc.
     */
    @PostMapping("/sell")
    public Map<String, Object> sellMedicine(@Valid @RequestBody SellMedicineRequest request) {
        OrderEvent event = OrderEvent.builder()
                .orderId(request.getOrderId())
                .medicineId(request.getMedicineId())
                .quantity(request.getQuantity())
                .timestamp(Instant.now())
                .build();
        orderEventProducer.publishStockDeduction(event).join();

        Double totalMoney = request.getTotalMoney();
        Double vatMoney = totalMoney * vatRate / 100;
        Double finalMoney = totalMoney + vatMoney;

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Thanh toán thành công");
        response.put("orderId", request.getOrderId());
        response.put("medicineId", request.getMedicineId());
        response.put("quantity", request.getQuantity());
        response.put("tongTienThuoc", totalMoney);
        response.put("vat", vatRate + "%");
        response.put("tienVAT", vatMoney);
        response.put("tongThanhToan", finalMoney);
        return response;
    }

    @PostMapping
    public Map<String, Object> calculateBill(
            @RequestBody BillRequest request
    ) {

        Double totalMoney = request.getTotalMoney();

        Double vatMoney = totalMoney * vatRate / 100;

        Double finalMoney = totalMoney + vatMoney;

        Map<String, Object> response = new HashMap<>();

        response.put("tongTienThuoc", totalMoney);
        response.put("vat", vatRate + "%");
        response.put("tienVAT", vatMoney);
        response.put("tongThanhToan", finalMoney);

        return response;
    }
}
