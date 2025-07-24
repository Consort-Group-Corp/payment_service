package uz.consortgroup.payment_service.service.handler.payme.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.payment_service.dto.paycom.CancelTransactionParams;
import uz.consortgroup.payment_service.dto.paycom.PaycomRequest;
import uz.consortgroup.payment_service.dto.paycom.PaycomResponse;
import uz.consortgroup.payment_service.entity.PaymeTransaction;
import uz.consortgroup.payment_service.entity.PaymeTransactionState;
import uz.consortgroup.payment_service.exception.paycom.OrderNotFoundException;
import uz.consortgroup.payment_service.exception.paycom.PaycomException;
import uz.consortgroup.payment_service.exception.paycom.UnableToCancelException;
import uz.consortgroup.payment_service.repository.PaymeTransactionRepository;
import uz.consortgroup.payment_service.service.handler.payme.PaycomMethodHandler;
import uz.consortgroup.payment_service.validator.PaymeTransactionValidatorService;

import java.time.Instant;
import java.util.Map;

import static uz.consortgroup.payment_service.service.util.JsonUtil.convertParams;
import static uz.consortgroup.payment_service.service.util.PaycomErrorFactory.internalError;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymeCancelTransactionHandler implements PaycomMethodHandler {

    private final PaymeTransactionRepository paymeTransactionRepository;
    private final PaymeTransactionValidatorService paymeTransactionValidatorService;

    @Override
    public String getMethod() {
        return "CancelTransaction";
    }

    @Override
    @Transactional
    public PaycomResponse handle(PaycomRequest request) {
        Object id = request.getId();
        log.info("Handling Payme CancelTransaction request: requestId={}", id);

        try {
            CancelTransactionParams params = convertParams(request.getParams(), CancelTransactionParams.class);
            String paycomTransactionId = params.getId();
            Integer reason = params.getReason();

            log.info("Parsed CancelTransactionParams: paycomTransactionId={}, reason={}", paycomTransactionId, reason);

            PaymeTransaction paymeTransaction = paymeTransactionRepository.findByPaycomTransactionId(paycomTransactionId)
                    .orElseThrow(() -> {
                        log.warn("Transaction not found: paycomTransactionId={}", paycomTransactionId);
                        return new OrderNotFoundException();
                    });

            if (paymeTransaction.getState() == PaymeTransactionState.CANCELED) {
                log.info("Transaction already canceled: id={}", paymeTransaction.getId());
                return PaycomResponse.success(id, Map.of(
                        "transaction", paymeTransaction.getId().toString(),
                        "cancel_time", paymeTransaction.getCancelTime().toEpochMilli(),
                        "state", paymeTransaction.getState().getCode()
                ));
            }

            if (paymeTransaction.getState() == PaymeTransactionState.COMPLETED) {
                log.warn("Unable to cancel completed transaction: id={}", paymeTransaction.getId());
                throw new UnableToCancelException();
            }

            paymeTransactionValidatorService.validateTransactionCancelable(paymeTransaction);
            log.info("Transaction is valid for cancellation: id={}", paymeTransaction.getId());

            paymeTransaction.setState(PaymeTransactionState.CANCELED);
            paymeTransaction.setCancelTime(Instant.now());
            paymeTransaction.setReason(reason);
            paymeTransactionRepository.save(paymeTransaction);

            log.info("Transaction successfully canceled: id={}", paymeTransaction.getId());

            return PaycomResponse.success(id, Map.of(
                    "transaction", paymeTransaction.getId().toString(),
                    "cancel_time", paymeTransaction.getCancelTime().toEpochMilli(),
                    "state", paymeTransaction.getState().getCode()
            ));

        } catch (PaycomException e) {
            return PaycomResponse.error(id, e);
        } catch (Exception e) {
            log.error("Unexpected error occurred during CancelTransaction: requestId={}", id, e);
            return PaycomResponse.error(id, internalError());
        }
    }
}
