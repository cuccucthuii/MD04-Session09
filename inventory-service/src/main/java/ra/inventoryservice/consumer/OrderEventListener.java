package ra.inventoryservice.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ra.inventoryservice.event.OrderEvent;
import ra.inventoryservice.service.InventoryOrderService;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final InventoryOrderService inventoryOrderService;

    @KafkaListener(
            topics = "${inventory.kafka.topic.stock-events}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "orderEventKafkaListenerContainerFactory"
    )
    public void onStockEvent(
            @Payload OrderEvent event,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String messageKey,
            @Header(KafkaHeaders.RECEIVED_PARTITION) Integer partition
    ) {
        log.info("[inventory] Consumer nhận message — partition={}, key={}, event={}",
                partition != null ? partition : "?", messageKey, event);
        try {
            inventoryOrderService.processOrderEvent(event);
        } catch (IllegalStateException e) {
            log.error("[inventory] Không xử lý được đơn: {}", e.getMessage());
        }
    }
}
