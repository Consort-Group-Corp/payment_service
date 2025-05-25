package uz.consortgroup.payment_service.service.handler.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.payment_service.asspect.annotation.AllAspect;
import uz.consortgroup.payment_service.dto.CheckTransactionDto;
import uz.consortgroup.payment_service.dto.PaycomRequest;
import uz.consortgroup.payment_service.dto.PaycomResponse;
import uz.consortgroup.payment_service.entity.Transaction;
import uz.consortgroup.payment_service.exception.TransactionNotFoundException;
import uz.consortgroup.payment_service.repository.TransactionRepository;
import uz.consortgroup.payment_service.service.handler.PaycomMethodHandler;

@Service
@RequiredArgsConstructor
public class CheckTransactionHandler implements PaycomMethodHandler {

    private final TransactionRepository transactionRepository;

    @Override
    public String getMethod() {
        return "CheckTransaction";
    }

    @Override
    @Transactional(readOnly = true)
    @AllAspect
    public PaycomResponse handle(PaycomRequest request) {
        Object id = request.getId();

        String paycomTransactionId = (String) request.getParams().get("id");

        Transaction tx = transactionRepository.findByPaycomTransactionId(paycomTransactionId)
                .orElseThrow(TransactionNotFoundException::new);

        CheckTransactionDto result = CheckTransactionDto.builder()
                .create_time(tx.getCreateTime().toEpochMilli())
                .perform_time(tx.getPerformTime() != null ? tx.getPerformTime().toEpochMilli() : null)
                .cancel_time(tx.getCancelTime() != null ? tx.getCancelTime().toEpochMilli() : null)
                .transaction(tx.getPaycomTransactionId())
                .state(tx.getState().getCode())
                .reason(tx.getReason())
                .build();

        return PaycomResponse.success(id, result);
    }
}
