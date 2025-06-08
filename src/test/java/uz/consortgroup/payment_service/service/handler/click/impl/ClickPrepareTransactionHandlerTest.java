package uz.consortgroup.payment_service.service.handler.click.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderSource;
import uz.consortgroup.payment_service.dto.click.ClickRequest;
import uz.consortgroup.payment_service.dto.click.ClickResponse;
import uz.consortgroup.payment_service.entity.ClickTransaction;
import uz.consortgroup.payment_service.entity.ClickTransactionState;
import uz.consortgroup.payment_service.entity.Order;
import uz.consortgroup.payment_service.exception.click.ClickException;
import uz.consortgroup.payment_service.repository.ClickTransactionRepository;
import uz.consortgroup.payment_service.validator.ClickTransactionValidatorService;
import uz.consortgroup.payment_service.validator.OrderValidatorService;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClickPrepareTransactionHandlerTest {

    @Mock
    private ClickTransactionRepository clickRepo;

    @Mock
    private OrderValidatorService orderValidator;

    @Mock
    private ClickTransactionValidatorService txValidator;

    private ClickPrepareTransactionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ClickPrepareTransactionHandler(clickRepo, orderValidator, txValidator);
    }

    @Test
    void handle_newTransaction_savesAndReturnsSuccess() {
        ClickRequest req = new ClickRequest();
        req.setClickTransactionId(1L);
        req.setServiceId(10L);
        req.setMerchantTransactionId("mti");
        req.setAmount(100L);
        req.setAction(0);
        req.setSignTime(Instant.now().toString());
        req.setSignString("sig");

        Order order = new Order();
        order.setSource(OrderSource.CLICK);
        order.setAmount(100L);

        when(clickRepo.existsByClickTransactionId(1L)).thenReturn(false);
        doNothing().when(txValidator).validateSignature(req);
        when(orderValidator.validateOrderExists("mti", OrderSource.CLICK)).thenReturn(order);
        doNothing().when(orderValidator).validateAmount(order, 100L);
        doNothing().when(orderValidator).validateOrderStatus(order);

        ClickResponse resp = handler.handle(req);

        assertThat(resp.getError()).isZero();
        assertThat(resp.getMerchant_prepare_id()).isNotBlank();
        verify(clickRepo).save(any(ClickTransaction.class));
    }

    @Test
    void handle_existingCompleted_throwsAlreadyPaid() {
        ClickRequest req = new ClickRequest(); req.setClickTransactionId(2L);
        ClickTransaction tx = new ClickTransaction();
        tx.setClickTransactionId(2L);
        tx.setState(ClickTransactionState.COMPLETED);

        when(clickRepo.existsByClickTransactionId(2L)).thenReturn(true);
        when(clickRepo.findByClickTransactionId(2L)).thenReturn(Optional.of(tx));
        doNothing().when(txValidator).validateSignature(req);

        assertThatThrownBy(() -> handler.handle(req))
            .isInstanceOf(ClickException.class)
            .hasMessageContaining("Order already paid");
    }

    @Test
    void handle_existingCreated_returnsSuccess() {
        ClickRequest req = new ClickRequest(); req.setClickTransactionId(3L);
        ClickTransaction tx = new ClickTransaction();
        tx.setClickTransactionId(3L);
        tx.setState(ClickTransactionState.CREATED);
        tx.setMerchantTransactionId("m3");
        tx.setMerchantPrepareId("mp3");

        when(clickRepo.existsByClickTransactionId(3L)).thenReturn(true);
        when(clickRepo.findByClickTransactionId(3L)).thenReturn(Optional.of(tx));
        doNothing().when(txValidator).validateSignature(req);

        ClickResponse resp = handler.handle(req);
        assertThat(resp.getError()).isZero();
        assertThat(resp.getMerchant_prepare_id()).isEqualTo("mp3");
    }

    @Test
    void handle_orderNotFound_throws() {
        ClickRequest req = new ClickRequest(); req.setClickTransactionId(4L);
        when(clickRepo.existsByClickTransactionId(4L)).thenReturn(false);
        doNothing().when(txValidator).validateSignature(req);
        doThrow(new ClickException(-6, Map.of("en", "Order not found")))
            .when(orderValidator).validateOrderExists("mti4", OrderSource.CLICK);
        req.setMerchantTransactionId("mti4"); req.setAmount(50L);

        assertThatThrownBy(() -> handler.handle(req))
            .isInstanceOf(ClickException.class)
            .hasMessageContaining("Order not found");
    }

    @Test
    void handle_incorrectAmount_throws() {
        ClickRequest req = new ClickRequest(); req.setClickTransactionId(5L);

        when(clickRepo.existsByClickTransactionId(5L)).thenReturn(false);
        doNothing().when(txValidator).validateSignature(req);

        Order order = new Order(); order.setAmount(100L);

        when(orderValidator.validateOrderExists("mti5", OrderSource.CLICK)).thenReturn(order);
        doThrow(new ClickException(-2, Map.of("en", "Incorrect amount")))
            .when(orderValidator).validateAmount(order, 200L);
        req.setMerchantTransactionId("mti5"); req.setAmount(200L);

        assertThatThrownBy(() -> handler.handle(req))
            .isInstanceOf(ClickException.class)
            .hasMessageContaining("Incorrect amount");
    }

    @Test
    void handle_invalidStatus_throws() {
        ClickRequest req = new ClickRequest(); req.setClickTransactionId(6L);
        when(clickRepo.existsByClickTransactionId(6L)).thenReturn(false);
        doNothing().when(txValidator).validateSignature(req);

        Order order = new Order(); order.setAmount(100L);

        when(orderValidator.validateOrderExists("mti6", OrderSource.CLICK)).thenReturn(order);
        doNothing().when(orderValidator).validateAmount(order, 100L);
        doThrow(new ClickException(-10, Map.of("en", "Incorrect request")))
            .when(orderValidator).validateOrderStatus(order);
        req.setMerchantTransactionId("mti6"); req.setAmount(100L);

        assertThatThrownBy(() -> handler.handle(req))
            .isInstanceOf(ClickException.class)
            .hasMessageContaining("Incorrect request");
    }
}
