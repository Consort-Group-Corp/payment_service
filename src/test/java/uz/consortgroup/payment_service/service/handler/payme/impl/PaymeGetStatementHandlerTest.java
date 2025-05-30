package uz.consortgroup.payment_service.service.handler.payme.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.consortgroup.payment_service.dto.paycom.GetStatementDto;
import uz.consortgroup.payment_service.dto.paycom.PaycomRequest;
import uz.consortgroup.payment_service.dto.paycom.PaycomResponse;
import uz.consortgroup.payment_service.entity.PaymeTransaction;
import uz.consortgroup.payment_service.entity.PaymeTransactionState;
import uz.consortgroup.payment_service.repository.PaymeTransactionRepository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymeGetStatementHandlerTest {

    @Mock
    PaymeTransactionRepository transactionRepository;

    @InjectMocks
    PaymeGetStatementHandler handler;

    @Test
    void handle_shouldReturnTransactionsWithinDateRange() {
        Instant from = Instant.parse("2024-01-01T00:00:00Z");
        Instant to = Instant.parse("2024-01-31T23:59:59Z");

        PaymeTransaction tx1 = PaymeTransaction.builder()
                .id(UUID.randomUUID())
                .paycomTransactionId("tx1")
                .orderId("order1")
                .amount(1000L)
                .state(PaymeTransactionState.CREATED)
                .createTime(from.plusSeconds(3600))
                .performTime(null)
                .cancelTime(null)
                .reason(null)
                .build();

        PaymeTransaction tx2 = PaymeTransaction.builder()
                .id(UUID.randomUUID())
                .paycomTransactionId("tx2")
                .orderId("order2")
                .amount(2000L)
                .state(PaymeTransactionState.COMPLETED)
                .createTime(from.plusSeconds(7200))
                .performTime(to.minusSeconds(1000))
                .cancelTime(null)
                .reason(-1)
                .build();

        when(transactionRepository.findAllByCreateTimeBetween(from, to))
                .thenReturn(List.of(tx1, tx2));

        PaycomRequest request = PaycomRequest.builder()
                .id(1)
                .method("GetStatement")
                .params(Map.of(
                        "from", from.toEpochMilli(),
                        "to", to.toEpochMilli()
                ))
                .build();

        PaycomResponse response = handler.handle(request);

        assertNotNull(response);
        assertEquals(1, response.getId());
        assertNull(response.getError());
        assertNotNull(response.getResult());

        @SuppressWarnings("unchecked")
        List<GetStatementDto> result = (List<GetStatementDto>) response.getResult();

        assertThat(result).hasSize(2);

        GetStatementDto first = result.get(0);
        assertThat(first.getTransaction()).isEqualTo("tx1");
        assertThat(first.getAmount()).isEqualTo(1000L);
        assertThat(first.getState()).isEqualTo(PaymeTransactionState.CREATED.getCode());
        assertThat(first.getAccount()).containsEntry("order_id", "order1");

        GetStatementDto second = result.get(1);
        assertThat(second.getTransaction()).isEqualTo("tx2");
        assertThat(second.getAmount()).isEqualTo(2000L);
        assertThat(second.getState()).isEqualTo(PaymeTransactionState.COMPLETED.getCode());
        assertThat(second.getAccount()).containsEntry("order_id", "order2");
        assertThat(second.getReason()).isEqualTo(-1);
    }


    @Test
    void handle_shouldReturnEmptyResultWhenNoTransactions() {
        Instant from = Instant.parse("2024-02-01T00:00:00Z");
        Instant to = Instant.parse("2024-02-01T01:00:00Z");

        when(transactionRepository.findAllByCreateTimeBetween(from, to))
                .thenReturn(List.of());

        PaycomRequest request = PaycomRequest.builder()
                .id(2)
                .method("GetStatement")
                .params(Map.of(
                        "from", from.toEpochMilli(),
                        "to", to.toEpochMilli()
                ))
                .build();

        PaycomResponse response = handler.handle(request);

        assertNotNull(response);
        assertEquals(2, response.getId());
        assertNull(response.getError());
        assertNotNull(response.getResult());

        @SuppressWarnings("unchecked")
        List<?> result = (List<?>) response.getResult();

        assertTrue(result.isEmpty());
    }
}
