package ra.inventoryservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ra.inventoryservice.dto.ImportStockRequest;
import ra.inventoryservice.dto.UpdateMedicinePriceRequest;
import ra.inventoryservice.entity.Medicine;
import ra.inventoryservice.service.MedicineService;

@RestController
@RequestMapping("/api/v1/medicines")
@RequiredArgsConstructor
public class MedicineController {

    private final MedicineService medicineService;

    @GetMapping("/{id}")
    public Medicine getMedicine(@PathVariable Long id) {
        return medicineService.getMedicineById(id);
    }

    @PutMapping("/{id}/price")
    public Medicine updateMedicinePrice(
            @PathVariable Long id,
            @RequestBody UpdateMedicinePriceRequest request) {
        Medicine medicine = new Medicine();
        medicine.setId(id);
        medicine.setPrice(request.getPrice());
        return medicineService.updateMedicine(medicine);
    }

    @PostMapping("/{id}/import")
    public Medicine importStock(
            @PathVariable Long id,
            @RequestBody ImportStockRequest request) {
        return medicineService.importStock(id, request.getQuantity());
    }

    /**
     * Demo race condition: 2 luồng cùng bán thuốc cuối cùng — chỉ 1 thành công nhờ Distributed Lock.
     */
    @PutMapping("/sell/{id}")
    public String sellMedicine(@PathVariable Long id) throws InterruptedException {
        Thread thread1 = new Thread(() -> {
            String rsBuy = medicineService.sellMedicine(id);
            System.out.println("Người dùng 1 : " + rsBuy);
        });

        Thread thread2 = new Thread(() -> {
            String rsBuy = medicineService.sellMedicine(id);
            System.out.println("Người dùng 2 : " + rsBuy);
        });

        thread2.start();
        thread1.start();
        thread1.join();
        thread2.join();

        return "Đã chạy 2 luồng bán hàng — xem log console";
    }
}
