package ra.inventoryservice.pubsub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import ra.inventoryservice.event.PharmacyAlert;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

@Slf4j
@RequiredArgsConstructor
public class PharmacyAlertSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final String channel;

    public void logSubscription() {
        log.info("[pharmacy-alerts] Subscriber đang lắng nghe channel={}", channel);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String body = new String(message.getBody(), StandardCharsets.UTF_8);

        log.info("[Dashboard] pharmacy-alerts (raw): {}", body);
        System.out.println("[Dashboard] pharmacy-alerts (raw): " + body);

        try {
            PharmacyAlert alert = objectMapper.readValue(body, PharmacyAlert.class);
            String line = String.format("[Dashboard] type=%s | %s", alert.getType(), alert.getMessage());
            log.info(line);
            System.out.println(line);
        } catch (JacksonException e) {
            log.warn("[pharmacy-alerts] Không parse JSON: {}", body, e);
        }
    }
}
