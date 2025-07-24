package uz.consortgroup.payment_service.service.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderRequest;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderResponse;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderSource;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderStatus;
import uz.consortgroup.payment_service.entity.Order;
import uz.consortgroup.payment_service.exception.OrderAlreadyExistsException;
import uz.consortgroup.payment_service.exception.OrderNotFoundException;
import uz.consortgroup.payment_service.mapper.OrderMapper;
import uz.consortgroup.payment_service.repository.OrderRepository;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderEventPublisherStrategy orderEventPublisherStrategy;

    @Transactional
    @Override
    public OrderResponse create(OrderRequest request) {
        log.info("Creating order: externalOrderId={}, userId={}, itemId={}, amount={}, source={}",
                request.getExternalOrderId(), request.getUserId(), request.getItemId(), request.getAmount(), request.getSource());

        if (orderRepository.findByExternalOrderIdAndSource(request.getExternalOrderId(), request.getSource()).isPresent()) {
            log.warn("Order already exists: externalOrderId={}, source={}", request.getExternalOrderId(), request.getSource());
            throw new OrderAlreadyExistsException("Order with ID " + request.getExternalOrderId() + " already exists");
        }

        Order order = Order.builder()
                .externalOrderId(request.getExternalOrderId())
                .itemId(request.getItemId())
                .userId(request.getUserId())
                .amount(request.getAmount())
                .itemType(request.getItemType())
                .source(request.getSource())
                .status(OrderStatus.NEW)
                .build();

        order = orderRepository.save(order);
        log.info("Order created successfully: orderId={}, externalOrderId={}", order.getId(), order.getExternalOrderId());

        return orderMapper.toDto(order);
    }

    @Override
    @Transactional
    public void markAsPaidAndPublish(String externalOrderId, OrderSource source) {
        log.info("Marking order as paid: externalOrderId={}, source={}", externalOrderId, source);

        Order order = orderRepository.findByExternalOrderIdAndSource(externalOrderId, source)
                .orElseThrow(() -> {
                    log.warn("Order not found for payment: externalOrderId={}, source={}", externalOrderId, source);
                    return new OrderNotFoundException("Order not found");
                });

        order.setStatus(OrderStatus.PAID);
        order.setUpdatedAt(Instant.now());

        orderRepository.save(order);
        log.info("Order marked as PAID and saved: orderId={}", order.getId());

        orderEventPublisherStrategy.sendEvent(order);
        log.info("Payment event published for orderId={}", order.getId());
    }

    @Override
    @Transactional
    public void deleteByExternalOrderId(String externalOrderId) {
        log.info("Deleting order by externalOrderId={}", externalOrderId);
        orderRepository.deleteByExternalOrderId(externalOrderId);
        log.info("Order deleted: externalOrderId={}", externalOrderId);
    }
}
