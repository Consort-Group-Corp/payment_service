package uz.consortgroup.payment_service.service.handler.payme.impl;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uz.consortgroup.payment_service.dto.paycom.PaycomRequest;
import uz.consortgroup.payment_service.dto.paycom.PaycomResponse;
import uz.consortgroup.payment_service.dto.paycom.PerformTransactionParams;
import uz.consortgroup.payment_service.entity.PaymeTransaction;
import uz.consortgroup.payment_service.entity.PaymeTransactionState;
import uz.consortgroup.payment_service.exception.paycom.TransactionNotFoundException;
import uz.consortgroup.payment_service.repository.PaymeTransactionRepository;
import uz.consortgroup.payment_service.validator.PaymeTransactionValidatorService;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymePerformTransactionHandlerTest {

    @Mock
    private PaymeTransactionRepository transactionRepository;

    @Mock
    private PaymeTransactionValidatorService transactionValidatorService;

    @InjectMocks
    private PaymePerformTransactionHandler handler;

    public PaymePerformTransactionHandlerTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handle_shouldCompleteTransactionWhenStateIsCreated() {
        String txId = "trans1";
        UUID uuid = UUID.randomUUID();
        Instant beforePerform = Instant.now();

        PerformTransactionParams params = new PerformTransactionParams();
        params.setId(txId);

        PaycomRequest request = PaycomRequest.builder()
                .id(10)
                .method("PerformTransaction")
                .params(Map.of("id", txId))
                .build();

        PaymeTransaction tx = PaymeTransaction.builder()
                .id(uuid)
                .paycomTransactionId(txId)
                .state(PaymeTransactionState.CREATED)
                .createTime(Instant.now())
                .build();

        when(transactionRepository.findByPaycomTransactionId(txId)).thenReturn(Optional.of(tx));

        doNothing().when(transactionValidatorService).validateTransactionState(tx, PaymeTransactionState.CREATED);

        ArgumentCaptor<PaymeTransaction> captor = ArgumentCaptor.forClass(PaymeTransaction.class);
        PaycomResponse response = handler.handle(request);

        verify(transactionRepository).save(captor.capture());
        PaymeTransaction savedTx = captor.getValue();

        assertEquals(PaymeTransactionState.COMPLETED, savedTx.getState());
        assertNotNull(savedTx.getPerformTime());
        assertThat(savedTx.getPerformTime()).isAfterOrEqualTo(beforePerform);

        assertEquals(10, response.getId());
        assertNull(response.getError());

        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) response.getResult();

        assertEquals(savedTx.getId().toString(), result.get("transaction"));
        assertEquals(PaymeTransactionState.COMPLETED.getCode(), result.get("state"));
        assertNotNull(result.get("perform_time"));
    }

    @Test
    void handle_shouldReturnSuccessWithoutChangingWhenTransactionAlreadyCompleted() {
        String txId = "trans2";
        UUID uuid = UUID.randomUUID();
        Instant performTime = Instant.now().minusSeconds(1000);

        PaymeTransaction tx = PaymeTransaction.builder()
                .id(uuid)
                .paycomTransactionId(txId)
                .state(PaymeTransactionState.COMPLETED)
                .performTime(performTime)
                .build();

        PaycomRequest request = PaycomRequest.builder()
                .id(11)
                .method("PerformTransaction")
                .params(Map.of("id", txId))
                .build();

        when(transactionRepository.findByPaycomTransactionId(txId)).thenReturn(Optional.of(tx));

        PaycomResponse response = handler.handle(request);

        verify(transactionRepository, never()).save(any());

        assertEquals(11, response.getId());
        assertNull(response.getError());

        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) response.getResult();

        assertEquals(tx.getId().toString(), result.get("transaction"));
        assertEquals(PaymeTransactionState.COMPLETED.getCode(), result.get("state"));
        assertEquals(performTime.toEpochMilli(), result.get("perform_time"));
    }

    @Test
    void handle_shouldThrowWhenTransactionNotFound() {
        PaycomRequest request = PaycomRequest.builder()
                .id(12)
                .method("PerformTransaction")
                .params(Map.of("id", "nonexistent"))
                .build();

        when(transactionRepository.findByPaycomTransactionId("nonexistent")).thenReturn(Optional.empty());

        assertThrows(TransactionNotFoundException.class, () -> handler.handle(request));
    }
}
