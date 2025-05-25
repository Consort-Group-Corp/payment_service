package uz.consortgroup.payment_service.service.handler.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.payment_service.asspect.annotation.AllAspect;
import uz.consortgroup.payment_service.dto.PaycomRequest;
import uz.consortgroup.payment_service.dto.PaycomResponse;
import uz.consortgroup.payment_service.dto.PerformTransactionParams;
import uz.consortgroup.payment_service.entity.Transaction;
import uz.consortgroup.payment_service.entity.TransactionState;
import uz.consortgroup.payment_service.exception.TransactionInvalidStateException;
import uz.consortgroup.payment_service.exception.TransactionNotFoundException;
import uz.consortgroup.payment_service.repository.TransactionRepository;
import uz.consortgroup.payment_service.service.handler.PaycomMethodHandler;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static uz.consortgroup.payment_service.service.util.JsonUtil.convertParams;
import static uz.consortgroup.payment_service.service.util.PaycomErrorFactory.orderNotFound;

@Service
@RequiredArgsConstructor
public class PerformTransactionHandler implements PaycomMethodHandler {

    private final TransactionRepository transactionRepository;

    @Override
    public String getMethod() {
        return "PerformTransaction";
    }

    @Override
    @Transactional
    @AllAspect
    public PaycomResponse handle(PaycomRequest request) {
        Object id = request.getId();

        PerformTransactionParams params = convertParams(request.getParams(), PerformTransactionParams.class);
        String paycomTransactionId = params.getId();

        Transaction transaction = transactionRepository.findByPaycomTransactionId(paycomTransactionId)
                .orElseThrow(TransactionNotFoundException::new);

        if (transaction.getState() == TransactionState.COMPLETED) {
            return PaycomResponse.success(id, Map.of(
                    "perform_time", transaction.getPerformTime() != null ? transaction.getPerformTime().toEpochMilli() : null,
                    "transaction", transaction.getId().toString(),
                    "state", transaction.getState().getCode()
            ));
        }

        if (transaction.getState() != TransactionState.CREATED) {
            throw new TransactionInvalidStateException();
        }

        transaction.setState(TransactionState.COMPLETED);
        transaction.setPerformTime(Instant.now());

        transactionRepository.save(transaction);

        Map<String, Object> result = Map.of(
                "perform_time", transaction.getPerformTime().toEpochMilli(),
                "transaction", transaction.getId().toString(),
                "state", transaction.getState().getCode()
        );

        return PaycomResponse.success(id, result);
    }

}
