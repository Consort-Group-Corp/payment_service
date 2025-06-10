package uz.consortgroup.payment_service.kafka;

import uz.consortgroup.core.api.v1.dto.payment.order.OrderItemType;
import uz.consortgroup.payment_service.entity.Order;

public interface OrderEventPublisher {
    boolean supports(OrderItemType type);
    void publish(Order order);
    void flush();
}
