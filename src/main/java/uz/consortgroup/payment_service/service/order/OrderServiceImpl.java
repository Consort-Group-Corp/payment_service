package uz.consortgroup.payment_service.service.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderRequest;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderResponse;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderSource;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderStatus;
import uz.consortgroup.payment_service.asspect.annotation.AllAspect;
import uz.consortgroup.payment_service.asspect.annotation.LoggingAspectAfterMethod;
import uz.consortgroup.payment_service.asspect.annotation.LoggingAspectBeforeMethod;
import uz.consortgroup.payment_service.entity.Order;
import uz.consortgroup.payment_service.exception.OrderAlreadyExistsException;
import uz.consortgroup.payment_service.exception.OrderNotFoundException;
import uz.consortgroup.payment_service.mapper.OrderMapper;
import uz.consortgroup.payment_service.repository.OrderRepository;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderEventPublisherStrategy orderEventPublisherStrategy;

    @Transactional
    @Override
    @AllAspect
    public OrderResponse create(OrderRequest request) {
        if (orderRepository.findByExternalOrderIdAndSource(request.getExternalOrderId(), request.getSource()).isPresent()) {
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
        return orderMapper.toDto(order);
    }

    @Override
    @Transactional
    @AllAspect
    public void markAsPaidAndPublish(String externalOrderId, OrderSource source) {
        Order order = orderRepository.findByExternalOrderIdAndSource(externalOrderId, source)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        order.setStatus(OrderStatus.PAID);
        order.setUpdatedAt(Instant.now());

        orderRepository.save(order);
        orderEventPublisherStrategy.sendEvent(order);
    }

    @Override
    @Transactional
    @LoggingAspectBeforeMethod
    @LoggingAspectAfterMethod
    public void deleteByExternalOrderId(String externalOrderId) {
        orderRepository.deleteByExternalOrderId(externalOrderId);
    }
}