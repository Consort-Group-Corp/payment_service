package uz.consortgroup.payment_service.service.handler.payme.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.consortgroup.payment_service.dto.paycom.PaycomRequest;
import uz.consortgroup.payment_service.dto.paycom.PaycomResponse;
import uz.consortgroup.payment_service.entity.PaymeTransaction;
import uz.consortgroup.payment_service.entity.PaymeTransactionState;
import uz.consortgroup.payment_service.repository.PaymeTransactionRepository;
import uz.consortgroup.payment_service.service.util.PaycomErrorFactory;
import uz.consortgroup.payment_service.validator.PaymeTransactionValidatorService;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymeCancelTransactionHandlerTest {

    @Mock
    private PaymeTransactionRepository paymeTransactionRepository;

    @Mock
    private PaymeTransactionValidatorService paymeTransactionValidatorService;

    @InjectMocks
    private PaymeCancelTransactionHandler handler;

    @Test
    void getMethod_shouldReturnCorrectMethodName() {
        assertEquals("CancelTransaction", handler.getMethod());
    }

    @Test
    void handle_shouldSuccessfullyCancelTransaction() {
        PaycomRequest request = new PaycomRequest();
        request.setId("req1");
        request.setParams(Map.of("id", "paycom123", "reason", 1));

        PaymeTransaction transaction = new PaymeTransaction();
        transaction.setId(UUID.randomUUID());
        transaction.setPaycomTransactionId("paycom123");
        transaction.setState(PaymeTransactionState.CREATED);

        when(paymeTransactionRepository.findByPaycomTransactionId("paycom123"))
                .thenReturn(Optional.of(transaction));

        PaycomResponse response = handler.handle(request);

        assertEquals("req1", response.getId());
        assertNull(response.getError());
        assertNotNull(response.getResult());
        verify(paymeTransactionRepository).save(any(PaymeTransaction.class));
    }

    @Test
    void handle_shouldReturnSuccessWhenTransactionAlreadyCanceled() {
        PaycomRequest request = new PaycomRequest();
        request.setId("req2");
        request.setParams(Map.of("id", "paycom124", "reason", 2));

        PaymeTransaction transaction = new PaymeTransaction();
        transaction.setId(UUID.randomUUID());
        transaction.setPaycomTransactionId("paycom124");
        transaction.setState(PaymeTransactionState.CANCELED);
        transaction.setCancelTime(Instant.now());

        when(paymeTransactionRepository.findByPaycomTransactionId("paycom124"))
                .thenReturn(Optional.of(transaction));

        PaycomResponse response = handler.handle(request);

        assertEquals("req2", response.getId());
        assertNull(response.getError());
        assertNotNull(response.getResult());
        verify(paymeTransactionRepository, never()).save(any());
    }

    @Test
    void handle_shouldReturnOrderNotFoundErrorWhenTransactionNotFound() {
        PaycomRequest request = new PaycomRequest();
        request.setId("req4");
        request.setParams(Map.of("id", "paycom126", "reason", 4));

        when(paymeTransactionRepository.findByPaycomTransactionId("paycom126"))
                .thenReturn(Optional.empty());

        PaycomResponse response = handler.handle(request);

        assertEquals("req4", response.getId());
        assertNotNull(response.getError());
        assertEquals(PaycomErrorFactory.orderNotFound().getCode(), response.getError().getCode());
    }

    @Test
    void handle_shouldReturnInternalErrorOnUnexpectedException() {
        PaycomRequest request = new PaycomRequest();
        request.setId("req5");
        request.setParams(Map.of("id", "paycom127", "reason", 5));

        when(paymeTransactionRepository.findByPaycomTransactionId("paycom127"))
                .thenThrow(new RuntimeException("Unexpected"));

        PaycomResponse response = handler.handle(request);

        assertEquals("req5", response.getId());
        assertNotNull(response.getError());
        assertEquals(PaycomErrorFactory.internalError().getCode(), response.getError().getCode());
    }
}
