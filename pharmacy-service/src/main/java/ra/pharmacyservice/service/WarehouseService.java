package ra.pharmacyservice.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final RestTemplate restTemplate;

    @CircuitBreaker(
            name = "warehouseCB",
            fallbackMethod = "fallbackCheckStock"
    )
    public String checkStock() {

        String url = "http://localhost:9090/api/v1/warehouse/check";

        return restTemplate.getForObject(url, String.class);
    }

    public String fallbackCheckStock(Exception ex) {

        return "Warehouse service is unavailable!";
    }
}