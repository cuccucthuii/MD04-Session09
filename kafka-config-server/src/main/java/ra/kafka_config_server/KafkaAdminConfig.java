package ra.kafka_config_server;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaAdminConfig {
    private final KafkaAdmin kafkaAdmin;

    /**
     * Sau khi app khởi động xong, in ra thông tin tất cả topics
     */
    @EventListener(ApplicationReadyEvent.class)
    public void describeTopics() {
        List<String> topics = List.of(
                TopicConstants.MEDICINE_STOCK_EVENTS,
                TopicConstants.MEDICINE_PRICE_UPDATES,
                TopicConstants.PHARMACY_NOTIFICATIONS
        );

        try (AdminClient client = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
            Map<String, TopicDescription> descriptions =
                    client.describeTopics(topics).allTopicNames().get();

            descriptions.forEach((name, desc) -> {
                log.info("=== Topic: {} | Partitions: {} ===",
                        name,
                        desc.partitions().size()
                );
                desc.partitions().forEach(p ->
                        log.info("  Partition {} | Leader: {} | Replicas: {}",
                                p.partition(),
                                p.leader().id(),
                                p.replicas().size()
                        )
                );
            });

        } catch (ExecutionException e) {
            if (e.getCause() instanceof UnknownTopicOrPartitionException) {
                log.warn("Một hoặc nhiều topic chưa sẵn sàng khi describe. " +
                        "Kiểm tra lại quá trình tạo topic hoặc thử lại sau vài giây.");
                return;
            }
            log.error("Không thể describe topics do lỗi khi gọi Kafka Admin API", e);
        } catch (Exception e) {
            log.error("Không thể kết nối Kafka để describe topics", e);
        }
    }
}
