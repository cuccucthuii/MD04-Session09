package ra.kafka_config_server;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {
    // Đọc từ application.yml
    @Value("${pharmacy.kafka.topics.stock-events.partitions:3}")
    private int stockPartitions;

    @Value("${pharmacy.kafka.topics.price-updates.partitions:1}")
    private int pricePartitions;

    @Value("${pharmacy.kafka.topics.notifications.partitions:2}")
    private int notifPartitions;

    @Value("${pharmacy.kafka.topics.stock-events.replication-factor:1}")
    private int replicationFactor;

    /**
     * Nhập/xuất kho — 3 partitions để xử lý song song
     */
    @Bean
    public NewTopic medicineStockEventsTopic() {
        return TopicBuilder.name(TopicConstants.MEDICINE_STOCK_EVENTS)
                .partitions(stockPartitions)
                .replicas(replicationFactor)
                .build();
    }

    /**
     * Cập nhật giá — 1 partition để đảm bảo thứ tự tuyệt đối
     */
    @Bean
    public NewTopic medicinePriceUpdatesTopic() {
        return TopicBuilder.name(TopicConstants.MEDICINE_PRICE_UPDATES)
                .partitions(pricePartitions)
                .replicas(replicationFactor)
                .build();
    }

    /**
     * Thông báo hệ thống — 2 partitions cân bằng tải
     */
    @Bean
    public NewTopic pharmacyNotificationsTopic() {
        return TopicBuilder.name(TopicConstants.PHARMACY_NOTIFICATIONS)
                .partitions(notifPartitions)
                .replicas(replicationFactor)
                .build();
    }
}
