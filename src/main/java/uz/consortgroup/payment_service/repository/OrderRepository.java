package uz.consortgroup.payment_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uz.consortgroup.payment_service.entity.Order;
import uz.consortgroup.payment_service.entity.OrderSource;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    Optional<Order> findByExternalOrderIdAndSource(String externalOrderId, OrderSource source);
    boolean existsByExternalOrderIdAndSource(String externalOrderId, OrderSource source);
}
