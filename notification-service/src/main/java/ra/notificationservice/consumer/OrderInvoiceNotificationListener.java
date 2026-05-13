package ra.notificationservice.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ra.notificationservice.event.OrderEvent;
import ra.notificationservice.service.InvoiceNotificationService;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderInvoiceNotificationListener {

    private final InvoiceNotificationService invoiceNotificationService;

    @KafkaListener(
            topics = "${notification.kafka.topic.stock-events}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "notificationKafkaListenerContainerFactory"
    )
    public void onOrderStockEvent(
            @Payload OrderEvent event,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String messageKey,
            @Header(KafkaHeaders.RECEIVED_PARTITION) Integer partition
    ) {
        log.debug("[notification] partition={}, key={}, payload={}", partition, messageKey, event);
        invoiceNotificationService.notifyInvoiceSent(event);
    }
}
