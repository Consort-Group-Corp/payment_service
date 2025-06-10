package uz.consortgroup.payment_service.service.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderItemType;
import uz.consortgroup.payment_service.entity.Order;
import uz.consortgroup.payment_service.event.coursepurchased.CoursePurchasedEvent;
import uz.consortgroup.payment_service.kafka.CoursePurchasedProducer;
import uz.consortgroup.payment_service.kafka.OrderEventPublisher;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CourseOrderEventPublisher implements OrderEventPublisher {
    private final CoursePurchasedProducer coursePurchasedProducer;
    private final List<Object> eventBatch = new ArrayList<>();

    @Override
    public boolean supports(OrderItemType type) {
        return OrderItemType.COURSE.equals(type);
    }

    @Override
    public void publish(Order order) {
        CoursePurchasedEvent event = CoursePurchasedEvent.builder()
                .messageId(UUID.randomUUID())
                .courseId(UUID.fromString(order.getExternalOrderId()))
                .userId(order.getUserId())
                .purchasedAt(order.getCreatedAt())
                .accessUntil(order.getUpdatedAt())
                .build();

        eventBatch.add(event);
        sendBatchIfNeeded();
    }

    @Override
    public void flush() {
        if (!eventBatch.isEmpty()) {
            coursePurchasedProducer.sendCoursePurchasedEvents(new ArrayList<>(eventBatch));
            eventBatch.clear();
        }
    }

    private void sendBatchIfNeeded() {
        if (eventBatch.size() >= 10) {
            coursePurchasedProducer.sendCoursePurchasedEvents(new ArrayList<>(eventBatch));
            eventBatch.clear();
        }
    }
}