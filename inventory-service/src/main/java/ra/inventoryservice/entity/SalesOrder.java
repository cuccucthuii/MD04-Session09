package ra.inventoryservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "sales_orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Trùng với {@link ra.inventoryservice.event.OrderEvent#getOrderId()} — idempotent consumer */
    @Column(name = "kafka_order_id", nullable = false, unique = true)
    private Long kafkaOrderId;

    @Column(nullable = false)
    private Long medicineId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Instant receivedAt;
}
