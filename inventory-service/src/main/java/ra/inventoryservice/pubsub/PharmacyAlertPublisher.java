package ra.inventoryservice.pubsub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import ra.inventoryservice.event.PharmacyAlert;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@RequiredArgsConstructor
public class PharmacyAlertPublisher {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final String channel;

    /**
     * Fire-and-forget: publish JSON lên channel, không lưu trữ message.
     */
    public void publish(PharmacyAlert alert) {
        try {
            String json = objectMapper.writeValueAsString(alert);
            stringRedisTemplate.convertAndSend(channel, json);
            log.info("[pub/sub] Đã publish channel={} payload={}", channel, json);
        } catch (JacksonException e) {
            throw new IllegalStateException("Không serialize PharmacyAlert", e);
        }
    }
}
