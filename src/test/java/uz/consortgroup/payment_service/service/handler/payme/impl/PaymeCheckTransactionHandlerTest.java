package uz.consortgroup.payment_service.service.handler.payme.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.consortgroup.payment_service.dto.paycom.CheckTransactionDto;
import uz.consortgroup.payment_service.dto.paycom.PaycomRequest;
import uz.consortgroup.payment_service.dto.paycom.PaycomResponse;
import uz.consortgroup.payment_service.entity.PaymeTransaction;
import uz.consortgroup.payment_service.entity.PaymeTransactionState;
import uz.consortgroup.payment_service.exception.paycom.TransactionNotFoundException;
import uz.consortgroup.payment_service.repository.PaymeTransactionRepository;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymeCheckTransactionHandlerTest {

    @Mock
    private PaymeTransactionRepository repo;

    @InjectMocks
    private PaymeCheckTransactionHandler handler;

    private PaymeTransaction tx;

    @BeforeEach
    void setUp() {
        tx = new PaymeTransaction();
        tx.setId(UUID.randomUUID());
        tx.setPaycomTransactionId("txn123");
        tx.setCreateTime(Instant.parse("2025-05-01T10:00:00Z"));
        tx.setPerformTime(Instant.parse("2025-05-02T11:00:00Z"));
        tx.setCancelTime(null);
        tx.setState(PaymeTransactionState.COMPLETED);
        tx.setReason(42);
    }

    @Test
    void getMethod_returnsCorrectName() {
        assertThat(handler.getMethod()).isEqualTo("CheckTransaction");
    }

    @Test
    void handle_existingTransaction_returnsDto() {
        when(repo.findByPaycomTransactionId("txn123")).thenReturn(Optional.of(tx));

        PaycomRequest req = new PaycomRequest();
        req.setId("req1");
        req.setParams(Map.of("id", "txn123"));

        PaycomResponse resp = handler.handle(req);

        assertThat(resp.getId()).isEqualTo("req1");
        assertThat(resp.getError()).isNull();

        Object result = resp.getResult();

        assertThat(result).isInstanceOf(CheckTransactionDto.class);
        CheckTransactionDto dto = (CheckTransactionDto) result;

        assertThat(dto.getTransaction()).isEqualTo("txn123");
        assertThat(dto.getCreate_time()).isEqualTo(tx.getCreateTime().toEpochMilli());
        assertThat(dto.getPerform_time()).isEqualTo(tx.getPerformTime().toEpochMilli());
        assertThat(dto.getCancel_time()).isNull();
        assertThat(dto.getState()).isEqualTo(PaymeTransactionState.COMPLETED.getCode());
        assertThat(dto.getReason()).isEqualTo(42);
    }

    @Test
    void handle_transactionNotFound_throws() {
        when(repo.findByPaycomTransactionId("missing")).thenReturn(Optional.empty());

        PaycomRequest req = new PaycomRequest();
        req.setId("req2");
        req.setParams(Map.of("id", "missing"));

        assertThatThrownBy(() -> handler.handle(req))
            .isInstanceOf(TransactionNotFoundException.class);
    }

    @Test
    void handle_nullRequest_throws() {
        assertThatThrownBy(() -> handler.handle(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void handle_missingIdParam_throwsTransactionNotFound() {
        PaycomRequest req = new PaycomRequest();
        req.setId("req3");
        req.setParams(Map.of());

        assertThatThrownBy(() -> handler.handle(req))
                .isInstanceOf(TransactionNotFoundException.class);
    }

    @Test
    void handle_invalidParamType_throws() {
        PaycomRequest req = new PaycomRequest();
        req.setId("req4");
        req.setParams(Map.of("id", 123));

        assertThatThrownBy(() -> handler.handle(req))
            .isInstanceOf(ClassCastException.class);
    }
}
