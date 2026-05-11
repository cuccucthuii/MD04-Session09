package ra.pharmacyservice.service;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ra.pharmacyservice.dto.BillRequest;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ElectronicInvoiceService {

    private final RestTemplate restTemplate;

    @Value("${pharmacy.e-invoice.gateway-url}")
    private String eInvoiceGatewayUrl;

    @RateLimiter(name = "eInvoiceRateLimit", fallbackMethod = "exportElectronicInvoiceFallback")
    @Retry(name = "eInvoiceRetry", fallbackMethod = "exportElectronicInvoiceFallback")
    public Map<String, Object> exportElectronicInvoice(BillRequest bill) {
        String reference = restTemplate.postForObject(eInvoiceGatewayUrl, bill, String.class);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "EXPORTED");
        body.put("eInvoiceReference", reference);
        body.put("tongTienThuoc", bill.getTotalMoney());
        return body;
    }

    public Map<String, Object> exportElectronicInvoiceFallback(BillRequest bill, Throwable cause) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "DEFERRED");
        body.put(
                "message",
                "Hệ thống xuất hóa đơn đang bận hoặc gặp sự cố mạng. Kết quả trả về ngay để không treo giao dịch; "
                        + "vui lòng không nhấn lặp liên tục — yêu cầu sẽ được xử lý lại theo cấu hình thử lại.");
        body.put("tongTienThuoc", bill != null ? bill.getTotalMoney() : null);
        return body;
    }
}
