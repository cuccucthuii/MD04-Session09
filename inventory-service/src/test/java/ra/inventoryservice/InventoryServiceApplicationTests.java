package ra.inventoryservice;

import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ra.inventoryservice.pubsub.PharmacyAlertPublisher;

@SpringBootTest
@EmbeddedKafka(
        partitions = 3,
        topics = {"medicine-stock-events"}
)
class InventoryServiceApplicationTests {

    @MockitoBean
    private PharmacyAlertPublisher pharmacyAlertPublisher;

    @MockitoBean
    private RedissonClient redissonClient;

    @Test
    void contextLoads() {
    }
}
