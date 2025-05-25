package uz.consortgroup.payment_service.service.handler.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.payment_service.asspect.annotation.AllAspect;
import uz.consortgroup.payment_service.dto.CancelTransactionParams;
import uz.consortgroup.payment_service.dto.PaycomRequest;
import uz.consortgroup.payment_service.dto.PaycomResponse;
import uz.consortgroup.payment_service.entity.Transaction;
import uz.consortgroup.payment_service.entity.TransactionState;
import uz.consortgroup.payment_service.exception.OrderNotFoundException;
import uz.consortgroup.payment_service.exception.PaycomException;
import uz.consortgroup.payment_service.exception.UnableToCancelException;
import uz.consortgroup.payment_service.repository.TransactionRepository;
import uz.consortgroup.payment_service.service.handler.PaycomMethodHandler;

import java.time.Instant;
import java.util.Map;

import static uz.consortgroup.payment_service.service.util.JsonUtil.convertParams;
import static uz.consortgroup.payment_service.service.util.PaycomErrorFactory.internalError;

@Service
@RequiredArgsConstructor
@Slf4j
public class CancelTransactionHandler implements PaycomMethodHandler {

    private final TransactionRepository transactionRepository;

    @Override
    public String getMethod() {
        return "CancelTransaction";
    }

    @Override
    @Transactional
    @AllAspect
    public PaycomResponse handle(PaycomRequest request) {
        Object id = request.getId();

        try {
            CancelTransactionParams params = convertParams(request.getParams(), CancelTransactionParams.class);
            String paycomTransactionId = params.getId();
            Integer reason = params.getReason();

            Transaction transaction = transactionRepository.findByPaycomTransactionId(paycomTransactionId)
                    .orElseThrow(OrderNotFoundException::new);

            if (transaction.getState() == TransactionState.CANCELED) {
                return PaycomResponse.success(id, Map.of(
                        "transaction", transaction.getId().toString(),
                        "cancel_time", transaction.getCancelTime().toEpochMilli(),
                        "state", transaction.getState().getCode()
                ));
            }

            if (transaction.getState() == TransactionState.COMPLETED) {
                throw new UnableToCancelException();
            }

            transaction.setState(TransactionState.CANCELED);
            transaction.setCancelTime(Instant.now());
            transaction.setReason(reason);

            transactionRepository.save(transaction);

            return PaycomResponse.success(id, Map.of(
                    "transaction", transaction.getId().toString(),
                    "cancel_time", transaction.getCancelTime().toEpochMilli(),
                    "state", transaction.getState().getCode()
            ));

        } catch (PaycomException e) {
            return PaycomResponse.error(id, e);
        } catch (Exception e) {
            return PaycomResponse.error(id, internalError());
        }
    }
}
