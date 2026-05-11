package ra.pharmacyservice.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ra.pharmacyservice.dto.BillRequest;

import java.util.HashMap;
import java.util.Map;

@RestController
@RefreshScope
@RequestMapping("/api/v1/bills")
public class BillController {
    @Value("${pharmacy.vat-rate}")
    private Double vatRate;

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
