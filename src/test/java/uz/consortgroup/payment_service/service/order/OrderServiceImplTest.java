package uz.consortgroup.payment_service.service.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uz.consortgroup.payment_service.dto.order.OrderRequest;
import uz.consortgroup.payment_service.dto.order.OrderResponse;
import uz.consortgroup.payment_service.entity.Order;
import uz.consortgroup.payment_service.entity.OrderSource;
import uz.consortgroup.payment_service.entity.OrderStatus;
import uz.consortgroup.payment_service.exception.OrderAlreadyExistsException;
import uz.consortgroup.payment_service.mapper.OrderMapper;
import uz.consortgroup.payment_service.repository.OrderRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void create_shouldSaveAndReturnOrder_whenOrderDoesNotExist() {
        OrderRequest request = new OrderRequest();
        request.setExternalOrderId("ext123");
        request.setAmount(5000L);
        request.setSource(OrderSource.CLICK);

        when(orderRepository.findByExternalOrderIdAndSource("ext123", OrderSource.CLICK))
                .thenReturn(Optional.empty());

        Order savedOrder = Order.builder()
                .externalOrderId("ext123")
                .amount(5000L)
                .source(OrderSource.CLICK)
                .status(OrderStatus.NEW)
                .build();

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        OrderResponse response = new OrderResponse();
        response.setExternalOrderId("ext123");
        response.setAmount(5000L);
        response.setSource(OrderSource.CLICK);
        response.setStatus(OrderStatus.NEW);

        when(orderMapper.toDto(any(Order.class))).thenReturn(response);

        OrderResponse result = orderService.create(request);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());

        Order captured = captor.getValue();
        assertEquals("ext123", captured.getExternalOrderId());
        assertEquals(5000L, captured.getAmount());
        assertEquals(OrderSource.CLICK, captured.getSource());
        assertEquals(OrderStatus.NEW, captured.getStatus());

        assertEquals(response, result);
    }

    @Test
    void create_shouldThrowException_whenOrderAlreadyExists() {
        OrderRequest request = new OrderRequest();
        request.setExternalOrderId("extExist");
        request.setAmount(1000L);
        request.setSource(OrderSource.PAYME);

        Order existingOrder = Order.builder()
                .externalOrderId("extExist")
                .amount(1000L)
                .source(OrderSource.PAYME)
                .status(OrderStatus.NEW)
                .build();

        when(orderRepository.findByExternalOrderIdAndSource("extExist", OrderSource.PAYME))
                .thenReturn(Optional.of(existingOrder));

        OrderAlreadyExistsException exception = assertThrows(OrderAlreadyExistsException.class,
                () -> orderService.create(request));

        assertThat(exception.getMessage())
                .isEqualTo("Order with ID extExist already exists");

        verify(orderRepository, never()).save(any());
    }
}
