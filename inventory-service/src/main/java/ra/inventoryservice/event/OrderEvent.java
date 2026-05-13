package ra.inventoryservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Khớp JSON do pharmacy-service gửi (JacksonJsonSerializer, không type header).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {
    private Long orderId;
    private Long medicineId;
    private Integer quantity;
    private Instant timestamp;
}
