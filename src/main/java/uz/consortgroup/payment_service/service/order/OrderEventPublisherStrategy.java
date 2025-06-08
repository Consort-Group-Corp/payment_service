package uz.consortgroup.payment_service.service.order;

import uz.consortgroup.payment_service.entity.Order;

public interface OrderEventPublisherStrategy {
    void sendEvent(Order order);
}
