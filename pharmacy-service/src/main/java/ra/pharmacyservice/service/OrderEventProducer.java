package ra.pharmacyservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import ra.pharmacyservice.event.OrderEvent;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class OrderEventProducer {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @Value("${pharmacy.kafka.topic.stock-events:medicine-stock-events}")
    private String stockEventsTopic;

    /**
     * Key = medicineId để các đơn cùng loại thuốc vào cùng một partition.
     */
    public CompletableFuture<SendResult<String, OrderEvent>> publishStockDeduction(OrderEvent event) {
        String key = String.valueOf(event.getMedicineId());
        return kafkaTemplate.send(stockEventsTopic, key, event);
    }
}
