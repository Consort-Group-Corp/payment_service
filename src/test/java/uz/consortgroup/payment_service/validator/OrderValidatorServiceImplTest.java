package uz.consortgroup.payment_service.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderSource;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderStatus;
import uz.consortgroup.payment_service.entity.Order;
import uz.consortgroup.payment_service.exception.AmountMismatchException;
import uz.consortgroup.payment_service.exception.OrderInvalidStatusException;
import uz.consortgroup.payment_service.exception.OrderNotFoundException;
import uz.consortgroup.payment_service.repository.OrderRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderValidatorServiceImplTest {

    private OrderRepository orderRepository;
    private OrderValidatorServiceImpl validator;

    @BeforeEach
    void setup() {
        orderRepository = mock(OrderRepository.class);
        validator = new OrderValidatorServiceImpl(orderRepository);
    }

    @Test
    void validateOrderExists_shouldReturnOrder_whenExists() {
        Order order = new Order();
        order.setExternalOrderId("ext123");
        order.setSource(OrderSource.CLICK);

        when(orderRepository.findByExternalOrderIdAndSource("ext123", OrderSource.CLICK))
                .thenReturn(Optional.of(order));

        Order result = validator.validateOrderExists("ext123", OrderSource.CLICK);

        assertEquals(order, result);
    }

    @Test
    void validateOrderExists_shouldThrow_whenNotFound() {
        when(orderRepository.findByExternalOrderIdAndSource("ext123", OrderSource.CLICK))
                .thenReturn(Optional.empty());

        OrderNotFoundException ex = assertThrows(OrderNotFoundException.class,
                () -> validator.validateOrderExists("ext123", OrderSource.CLICK));
        assertEquals("Order not found", ex.getMessage());
    }

    @Test
    void validateAmount_shouldNotThrow_whenAmountsMatch() {
        Order order = new Order();
        order.setAmount(1000L);

        assertDoesNotThrow(() -> validator.validateAmount(order, 1000L));
    }

    @Test
    void validateAmount_shouldThrow_whenAmountsDoNotMatch() {
        Order order = new Order();
        order.setAmount(1000L);

        AmountMismatchException ex = assertThrows(AmountMismatchException.class,
                () -> validator.validateAmount(order, 999L));
        assertEquals("Transaction amount does not match order amount", ex.getMessage());
    }

    @Test
    void validateOrderStatus_shouldNotThrow_whenStatusIsPayable() {
        Order order = new Order();
        order.setStatus(OrderStatus.NEW);

        assertDoesNotThrow(() -> validator.validateOrderStatus(order));
    }

    @Test
    void validateOrderStatus_shouldThrow_whenStatusIsNull() {
        Order order = new Order();
        order.setStatus(null);

        OrderInvalidStatusException ex = assertThrows(OrderInvalidStatusException.class,
                () -> validator.validateOrderStatus(order));
        assertEquals("Order status does not allow payment", ex.getMessage());
    }

    @Test
    void validateOrderStatus_shouldThrow_whenStatusIsNotPayable() {
        Order order = new Order();
        order.setStatus(OrderStatus.CANCELLED);

        OrderInvalidStatusException ex = assertThrows(OrderInvalidStatusException.class,
                () -> validator.validateOrderStatus(order));
        assertEquals("Order status does not allow payment", ex.getMessage());
    }
}
