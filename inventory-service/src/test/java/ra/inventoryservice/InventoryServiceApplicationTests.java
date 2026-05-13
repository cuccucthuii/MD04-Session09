package ra.inventoryservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;

@SpringBootTest
@EmbeddedKafka(
        partitions = 3,
        topics = {"medicine-stock-events"}
)
class InventoryServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
