package uz.consortgroup.payment_service.service.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.payment_service.asspect.annotation.AllAspect;
import uz.consortgroup.payment_service.dto.order.OrderRequest;
import uz.consortgroup.payment_service.dto.order.OrderResponse;
import uz.consortgroup.payment_service.entity.Order;
import uz.consortgroup.payment_service.entity.OrderStatus;
import uz.consortgroup.payment_service.exception.OrderAlreadyExistsException;
import uz.consortgroup.payment_service.mapper.OrderMapper;
import uz.consortgroup.payment_service.repository.OrderRepository;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Transactional
    @Override
    @AllAspect
    public OrderResponse create(OrderRequest request) {
        if (orderRepository.findByExternalOrderIdAndSource(request.getExternalOrderId(), request.getSource()).isPresent()) {
            throw new OrderAlreadyExistsException("Order with ID " + request.getExternalOrderId() + " already exists");
        }

        Order order = Order.builder()
                .externalOrderId(request.getExternalOrderId())
                .amount(request.getAmount())
                .source(request.getSource())
                .status(OrderStatus.NEW)
                .build();

        orderRepository.save(order);
        return orderMapper.toDto(order);
    }
}
