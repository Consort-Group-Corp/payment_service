package uz.consortgroup.payment_service.service.handler.payme.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.consortgroup.payment_service.dto.paycom.PaycomRequest;
import uz.consortgroup.payment_service.dto.paycom.PaycomResponse;
import uz.consortgroup.payment_service.entity.Order;
import uz.consortgroup.payment_service.entity.OrderSource;
import uz.consortgroup.payment_service.entity.PaymeTransaction;
import uz.consortgroup.payment_service.entity.PaymeTransactionState;
import uz.consortgroup.payment_service.exception.paycom.OrderNotFoundException;
import uz.consortgroup.payment_service.repository.PaymeTransactionRepository;
import uz.consortgroup.payment_service.validator.OrderValidatorService;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymeCreateTransactionHandlerTest {

    @InjectMocks
    private PaymeCreateTransactionHandler handler;

    @Mock
    private PaymeTransactionRepository transactionRepository;

    @Mock
    private OrderValidatorService orderValidatorService;

    @Test
    void handle_shouldReturnSuccessWhenTransactionAlreadyExists() {
        UUID txId = UUID.randomUUID();
        Instant createTime = Instant.ofEpochMilli(1620000000000L);

        PaymeTransaction existingTx = PaymeTransaction.builder()
                .id(txId)
                .paycomTransactionId("trans123")
                .orderId("order123")
                .amount(1000L)
                .state(PaymeTransactionState.CREATED)
                .createTime(createTime)
                .build();

        PaycomRequest request = PaycomRequest.builder()
                .id(1)
                .method("CreateTransaction")
                .params(Map.of(
                        "id", "trans123",
                        "account", Map.of("order_id", "order123"),
                        "amount", 1000L
                ))
                .build();

        when(transactionRepository.findByPaycomTransactionId("trans123"))
                .thenReturn(Optional.of(existingTx));

        PaycomResponse response = handler.handle(request);

        assertNotNull(response);
        assertEquals(1, response.getId());
        assertNull(response.getError());

        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) response.getResult();

        assertThat(result)
                .containsEntry("transaction", txId.toString())
                .containsEntry("create_time", createTime.toEpochMilli())
                .containsEntry("state", PaymeTransactionState.CREATED.getCode());

        verify(transactionRepository, never()).save(any());
    }


    @Test
    void handle_shouldReturnSuccessWhenCreatingNewTransaction() {
        PaycomRequest request = PaycomRequest.builder()
                .id(1)
                .method("CreateTransaction")
                .params(Map.of(
                        "id", "transNew",
                        "account", Map.of("order_id", "order456"),
                        "amount", 2000L
                ))
                .build();

        when(transactionRepository.findByPaycomTransactionId("transNew"))
                .thenReturn(Optional.empty());

        UUID orderUuid = UUID.randomUUID();
        Order order = Order.builder()
                .id(orderUuid)
                .build();

        when(orderValidatorService.validateOrderExists("order456", OrderSource.PAYME))
                .thenReturn(order);

        Instant now = Instant.now();

        when(transactionRepository.save(any(PaymeTransaction.class)))
                .thenAnswer(invocation -> {
                    PaymeTransaction tx = invocation.getArgument(0);
                    tx.setId(UUID.randomUUID());
                    tx.setCreateTime(now);
                    return tx;
                });

        PaycomResponse response = handler.handle(request);

        assertEquals(1, response.getId());
        assertNull(response.getError());
        assertNotNull(response.getResult());

        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) response.getResult();

        assertThat(result)
                .containsEntry("create_time", now.toEpochMilli())
                .containsEntry("state", PaymeTransactionState.CREATED.getCode());
        assertThat(result.get("transaction")).isInstanceOf(String.class);

        ArgumentCaptor<PaymeTransaction> captor = ArgumentCaptor.forClass(PaymeTransaction.class);
        verify(transactionRepository).save(captor.capture());

        PaymeTransaction capturedTx = captor.getValue();
        assertEquals("transNew", capturedTx.getPaycomTransactionId());
        assertEquals("order456", capturedTx.getOrderId());
        assertEquals(2000L, capturedTx.getAmount());
        assertEquals(PaymeTransactionState.CREATED, capturedTx.getState());
    }



    @Test
    void handle_shouldReturnErrorWhenOrderNotFound() {
        PaycomRequest request = PaycomRequest.builder()
                .id(1)
                .method("CreateTransaction")
                .params(Map.of(
                        "id", "trans123",
                        "account", Map.of("order_id", "order123"),
                        "amount", 1000L
                ))
                .build();

        when(transactionRepository.findByPaycomTransactionId("trans123"))
                .thenReturn(Optional.empty());

        when(orderValidatorService.validateOrderExists("order123", OrderSource.PAYME))
                .thenThrow(new OrderNotFoundException());

        assertThrows(OrderNotFoundException.class, () -> handler.handle(request));
    }
}
