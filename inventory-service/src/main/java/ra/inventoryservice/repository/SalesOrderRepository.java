package ra.inventoryservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ra.inventoryservice.entity.SalesOrder;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

    boolean existsByKafkaOrderId(Long kafkaOrderId);
}
