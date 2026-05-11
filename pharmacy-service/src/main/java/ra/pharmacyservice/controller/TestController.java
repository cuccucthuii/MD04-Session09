package ra.pharmacyservice.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ra.pharmacyservice.service.WarehouseService;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final WarehouseService warehouseService;

    @GetMapping("/check-stock")
    public String checkStock() {

        return warehouseService.checkStock();
    }

    @GetMapping("/api/v1/warehouse/check")
    public String check() throws Exception {

        Thread.sleep(10000);

        return "OK";
    }
}