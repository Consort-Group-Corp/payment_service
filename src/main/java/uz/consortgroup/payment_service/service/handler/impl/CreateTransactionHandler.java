package uz.consortgroup.payment_service.service.handler.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.payment_service.asspect.annotation.AllAspect;
import uz.consortgroup.payment_service.dto.CreateTransactionParams;
import uz.consortgroup.payment_service.dto.PaycomRequest;
import uz.consortgroup.payment_service.dto.PaycomResponse;
import uz.consortgroup.payment_service.entity.Transaction;
import uz.consortgroup.payment_service.entity.TransactionState;
import uz.consortgroup.payment_service.repository.TransactionRepository;
import uz.consortgroup.payment_service.service.handler.PaycomMethodHandler;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static uz.consortgroup.payment_service.service.util.JsonUtil.convertParams;

@Service
@RequiredArgsConstructor
public class CreateTransactionHandler implements PaycomMethodHandler {

    private final TransactionRepository transactionRepository;

    @Override
    public String getMethod() {
        return "CreateTransaction";
    }

    @Override
    @Transactional
    @AllAspect
    public PaycomResponse handle(PaycomRequest request) {
        Object id = request.getId();

        CreateTransactionParams params = convertParams(request.getParams(), CreateTransactionParams.class);

        Optional<Transaction> existing = transactionRepository.findByPaycomTransactionId(params.getId());

        if (existing.isPresent()) {
            return PaycomResponse.success(id, Map.of(
                    "create_time", existing.get().getCreateTime().toEpochMilli(),
                    "transaction", existing.get().getId().toString(),
                    "state", existing.get().getState().getCode()
            ));
        }

        Transaction transaction = Transaction.builder()
                .paycomTransactionId(params.getId())
                .orderId(params.getAccount().getOrderId())
                .amount(params.getAmount())
                .state(TransactionState.CREATED)
                .createTime(Instant.now())
                .build();

        transactionRepository.save(transaction);

        Map<String, Object> result = Map.of(
                "create_time", transaction.getCreateTime().toEpochMilli(),
                "transaction", transaction.getId().toString(),
                "state", transaction.getState().getCode()
        );

        return PaycomResponse.success(id, result);
    }

}
