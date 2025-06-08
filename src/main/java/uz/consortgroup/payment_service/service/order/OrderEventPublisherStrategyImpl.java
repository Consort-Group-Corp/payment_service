package uz.consortgroup.payment_service.service.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.consortgroup.payment_service.asspect.annotation.AllAspect;
import uz.consortgroup.payment_service.entity.Order;
import uz.consortgroup.payment_service.kafka.OrderEventPublisher;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisherStrategyImpl implements OrderEventPublisherStrategy {
    private final List<OrderEventPublisher> publishers;

    @AllAspect
    public void sendEvent(Order order) {
        log.info("Attempting to send event for order: {}, itemType: {}", order.getExternalOrderId(), order.getItemType());
        publishers.stream()
                .filter(p -> {
                    boolean supported = p.supports(order.getItemType());
                    log.info("Publisher supports itemType {}: {}", order.getItemType(), supported);
                    return supported;
                })
                .findFirst()
                .ifPresent(p -> {
                    log.info("Publishing event for order: {}", order.getExternalOrderId());
                    p.publish(order);
                    p.flush();
                });
    }
}