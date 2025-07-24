package uz.consortgroup.payment_service.service.handler.payme.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.payment_service.dto.paycom.GetStatementDto;
import uz.consortgroup.payment_service.dto.paycom.PaycomRequest;
import uz.consortgroup.payment_service.dto.paycom.PaycomResponse;
import uz.consortgroup.payment_service.entity.PaymeTransaction;
import uz.consortgroup.payment_service.repository.PaymeTransactionRepository;
import uz.consortgroup.payment_service.service.handler.payme.PaycomMethodHandler;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymeGetStatementHandler implements PaycomMethodHandler {

    private final PaymeTransactionRepository paymeTransactionRepository;

    @Override
    public String getMethod() {
        return "GetStatement";
    }

    @Override
    @Transactional(readOnly = true)
    public PaycomResponse handle(PaycomRequest request) {
        Object id = request.getId();

        Map<String, Object> params = request.getParams();
        long fromMillis = ((Number) params.get("from")).longValue();
        long toMillis = ((Number) params.get("to")).longValue();

        Instant from = Instant.ofEpochMilli(fromMillis);
        Instant to = Instant.ofEpochMilli(toMillis);

        log.info("Handling GetStatement request: requestId={}, from={}, to={}", id, from, to);

        List<PaymeTransaction> paymeTransactions = paymeTransactionRepository.findAllByCreateTimeBetween(from, to);
        log.info("Found {} transactions between {} and {}", paymeTransactions.size(), from, to);

        List<GetStatementDto> result = paymeTransactions.stream()
                .map(tx -> GetStatementDto.builder()
                        .transaction(tx.getPaycomTransactionId())
                        .time(tx.getPerformTime() != null ? tx.getPerformTime().toEpochMilli() : tx.getCreateTime().toEpochMilli())
                        .amount(tx.getAmount())
                        .account(Map.of("order_id", tx.getOrderId()))
                        .create_time(tx.getCreateTime().toEpochMilli())
                        .perform_time(tx.getPerformTime() != null ? tx.getPerformTime().toEpochMilli() : null)
                        .cancel_time(tx.getCancelTime() != null ? tx.getCancelTime().toEpochMilli() : null)
                        .state(tx.getState().getCode())
                        .reason(tx.getReason())
                        .build())
                .toList();

        log.info("Returning GetStatement response with {} entries for requestId={}", result.size(), id);

        return PaycomResponse.success(id, result);
    }
}
