package uz.consortgroup.payment_service.service.handler.click.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderSource;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderStatus;
import uz.consortgroup.payment_service.dto.click.ClickRequest;
import uz.consortgroup.payment_service.dto.click.ClickResponse;
import uz.consortgroup.payment_service.entity.ClickTransaction;
import uz.consortgroup.payment_service.entity.ClickTransactionState;
import uz.consortgroup.payment_service.entity.Order;
import uz.consortgroup.payment_service.exception.click.ClickException;
import uz.consortgroup.payment_service.repository.ClickTransactionRepository;
import uz.consortgroup.payment_service.repository.OrderRepository;
import uz.consortgroup.payment_service.validator.ClickTransactionValidatorService;
import uz.consortgroup.payment_service.validator.OrderValidatorService;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClickPaymentTransactionHandlerTest {

    @Mock
    private ClickTransactionRepository clickRepo;

    @Mock
    private OrderValidatorService orderValidator;

    @Mock
    private ClickTransactionValidatorService txValidator;

    @Mock
    private OrderRepository orderRepo;

    private ClickPaymentTransactionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ClickPaymentTransactionHandler(clickRepo, orderValidator, txValidator, orderRepo, null);
    }


    @Test
    void handle_transactionNotFound_throws() {
        ClickRequest req = new ClickRequest();
        req.setClickTransactionId(2L);
        doNothing().when(txValidator).validateSignature(req);
        when(clickRepo.findByClickTransactionId(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(req))
            .isInstanceOf(ClickException.class)
            .hasMessageContaining("Transaction not found");
    }

    @Test
    void handle_invalidSignature_throws() {
        ClickRequest req = new ClickRequest();
        req.setClickTransactionId(3L);
        doThrow(new ClickException(-1, Map.of("en", "SIGN CHECK FAILED")))
            .when(txValidator).validateSignature(req);

        assertThatThrownBy(() -> handler.handle(req))
            .isInstanceOf(ClickException.class)
            .hasMessageContaining("SIGN CHECK FAILED");
    }

    @Test
    void handle_invalidState_throws() {
        ClickRequest req = new ClickRequest();
        req.setClickTransactionId(4L);
        ClickTransaction tx = new ClickTransaction();
        tx.setClickTransactionId(4L);

        when(clickRepo.findByClickTransactionId(4L)).thenReturn(Optional.of(tx));
        doNothing().when(txValidator).validateSignature(req);

        doThrow(new ClickException(-9, Map.of("en", "Transaction is not confirmed")))
            .when(txValidator).validateTransactionState(tx, ClickTransactionState.CREATED);

        assertThatThrownBy(() -> handler.handle(req))
            .isInstanceOf(ClickException.class)
            .hasMessageContaining("Transaction is not confirmed");
    }

    @Test
    void handle_prepareIdMismatch_throws() {
        ClickRequest req = new ClickRequest();
        req.setClickTransactionId(5L);
        req.setMerchantPrepareId("wrong");

        ClickTransaction tx = new ClickTransaction();
        tx.setClickTransactionId(5L);
        tx.setMerchantPrepareId("mp5");

        when(clickRepo.findByClickTransactionId(5L)).thenReturn(Optional.of(tx));
        doNothing().when(txValidator).validateSignature(req);
        doNothing().when(txValidator).validateTransactionState(tx, ClickTransactionState.CREATED);

        assertThatThrownBy(() -> handler.handle(req))
            .isInstanceOf(ClickException.class)
            .hasMessageContaining("Error in request");
    }

    @Test
    void handle_orderNotFound_throws() {
        ClickRequest req = new ClickRequest();
        req.setClickTransactionId(6L);
        req.setMerchantPrepareId("mp6");
        req.setMerchantTransactionId("m6");

        ClickTransaction tx = new ClickTransaction();
        tx.setClickTransactionId(6L);
        tx.setMerchantPrepareId("mp6");
        tx.setState(ClickTransactionState.CREATED);

        when(clickRepo.findByClickTransactionId(6L)).thenReturn(Optional.of(tx));
        doNothing().when(txValidator).validateSignature(req);
        doNothing().when(txValidator).validateTransactionState(tx, ClickTransactionState.CREATED);

        doThrow(new ClickException(-6, Map.of("en", "Order not found")))
            .when(orderValidator).validateOrderExists("m6", OrderSource.CLICK);

        assertThatThrownBy(() -> handler.handle(req))
            .isInstanceOf(ClickException.class)
            .hasMessageContaining("Order not found");
    }

    @Test
    void handle_incorrectAmount_throws() {
        ClickRequest req = new ClickRequest();
        req.setClickTransactionId(7L);
        req.setMerchantPrepareId("mp7");
        req.setMerchantTransactionId("m7");
        req.setAmount(200L);

        ClickTransaction tx = new ClickTransaction();
        tx.setClickTransactionId(7L);
        tx.setMerchantPrepareId("mp7");
        tx.setState(ClickTransactionState.CREATED);
        Order order = new Order();
        order.setAmount(100L);

        when(clickRepo.findByClickTransactionId(7L)).thenReturn(Optional.of(tx));
        doNothing().when(txValidator).validateSignature(req);
        doNothing().when(txValidator).validateTransactionState(tx, ClickTransactionState.CREATED);
        when(orderValidator.validateOrderExists("m7", OrderSource.CLICK)).thenReturn(order);
        doThrow(new ClickException(-2, Map.of("en", "Incorrect amount")))
            .when(orderValidator).validateAmount(order, 200L);

        assertThatThrownBy(() -> handler.handle(req))
            .isInstanceOf(ClickException.class)
            .hasMessageContaining("Incorrect amount");
    }

    @Test
    void handle_invalidOrderStatus_throws() {
        ClickRequest req = new ClickRequest();
        req.setClickTransactionId(8L);
        req.setMerchantPrepareId("mp8");
        req.setMerchantTransactionId("m8");
        req.setAmount(300L);

        ClickTransaction tx = new ClickTransaction();
        tx.setClickTransactionId(8L);
        tx.setMerchantPrepareId("mp8");
        tx.setState(ClickTransactionState.CREATED);
        Order order = new Order();
        order.setAmount(300L);

        when(clickRepo.findByClickTransactionId(8L)).thenReturn(Optional.of(tx));
        doNothing().when(txValidator).validateSignature(req);
        doNothing().when(txValidator).validateTransactionState(tx, ClickTransactionState.CREATED);
        when(orderValidator.validateOrderExists("m8", OrderSource.CLICK)).thenReturn(order);
        doNothing().when(orderValidator).validateAmount(order, 300L);
        doThrow(new ClickException(-10, Map.of("en", "Incorrect request")))
            .when(orderValidator).validateOrderStatus(order);

        assertThatThrownBy(() -> handler.handle(req))
            .isInstanceOf(ClickException.class)
            .hasMessageContaining("Incorrect request");
    }
}
