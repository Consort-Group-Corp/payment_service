package uz.consortgroup.payment_service.service.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderItemType;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderRequest;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderResponse;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderSource;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderStatus;
import uz.consortgroup.core.api.v1.dto.user.response.EligibilityResponse;
import uz.consortgroup.payment_service.client.UserClient;
import uz.consortgroup.payment_service.entity.Order;
import uz.consortgroup.payment_service.exception.OrderAlreadyExistsException;
import uz.consortgroup.payment_service.mapper.OrderMapper;
import uz.consortgroup.payment_service.repository.OrderRepository;
import uz.consortgroup.payment_service.security.AuthContext;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private UserClient userClient;

    @Mock
    private AuthContext authContext;

    @InjectMocks
    private OrderServiceImpl orderService;

    private UUID currentUserId;

    @BeforeEach
    void setUp() {
        currentUserId = UUID.randomUUID();
        when(authContext.getCurrentUserId()).thenReturn(currentUserId);
    }

    @Test
    void create_shouldSaveAndReturnOrder_whenOrderDoesNotExist() {
        OrderRequest request = new OrderRequest();
        request.setItemId(UUID.randomUUID());
        request.setExternalOrderId("ext123");
        request.setAmount(5000L);
        request.setSource(OrderSource.CLICK);
        request.setItemType(OrderItemType.COURSE);

        when(orderRepository.findByExternalOrderIdAndSource("ext123", OrderSource.CLICK))
                .thenReturn(Optional.empty());

        when(userClient.checkEligibility(eq(currentUserId), eq(request.getItemId())))
                .thenReturn(new EligibilityResponse(true, null, null));

        Order savedOrder = Order.builder()
                .externalOrderId("ext123")
                .itemId(request.getItemId())
                .userId(currentUserId)
                .amount(5000L)
                .itemType(OrderItemType.COURSE)
                .source(OrderSource.CLICK)
                .status(OrderStatus.NEW)
                .build();
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        OrderResponse mapped = OrderResponse.builder()
                .id(UUID.randomUUID())
                .externalOrderId("ext123")
                .userId(currentUserId)
                .itemId(request.getItemId())
                .amount(5000L)
                .itemType(OrderItemType.COURSE)
                .source(OrderSource.CLICK)
                .status(OrderStatus.NEW)
                .build();
        when(orderMapper.toDto(any(Order.class))).thenReturn(mapped);

        OrderResponse result = orderService.create(request);

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        Order captured = captor.getValue();
        assertEquals("ext123", captured.getExternalOrderId());
        assertEquals(5000L, captured.getAmount());
        assertEquals(OrderSource.CLICK, captured.getSource());
        assertEquals(OrderStatus.NEW, captured.getStatus());
        assertEquals(currentUserId, captured.getUserId());
        assertEquals(request.getItemId(), captured.getItemId());
        assertEquals(OrderItemType.COURSE, captured.getItemType());

        verify(userClient).checkEligibility(currentUserId, request.getItemId());

        assertEquals(mapped, result);
    }

    @Test
    void create_shouldThrowException_whenOrderAlreadyExists() {
        OrderRequest request = new OrderRequest();
        request.setExternalOrderId("extExist");
        request.setItemId(UUID.randomUUID());
        request.setAmount(1000L);
        request.setSource(OrderSource.PAYME);
        request.setItemType(OrderItemType.COURSE);

        Order existingOrder = Order.builder()
                .externalOrderId("extExist")
                .amount(1000L)
                .source(OrderSource.PAYME)
                .status(OrderStatus.NEW)
                .build();

        when(orderRepository.findByExternalOrderIdAndSource("extExist", OrderSource.PAYME))
                .thenReturn(Optional.of(existingOrder));

        OrderAlreadyExistsException ex = assertThrows(
                OrderAlreadyExistsException.class,
                () -> orderService.create(request)
        );
        assertThat(ex.getMessage()).isEqualTo("Order with ID extExist already exists");

        verify(userClient, never()).checkEligibility(any(UUID.class), any(UUID.class));
        verify(orderRepository, never()).save(any());
    }
}
