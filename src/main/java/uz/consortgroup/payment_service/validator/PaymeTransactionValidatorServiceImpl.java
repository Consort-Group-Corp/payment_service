package uz.consortgroup.payment_service.validator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uz.consortgroup.payment_service.entity.PaymeTransaction;
import uz.consortgroup.payment_service.entity.PaymeTransactionState;
import uz.consortgroup.payment_service.exception.TransactionAlreadyCanceledException;
import uz.consortgroup.payment_service.exception.TransactionNotFoundException;
import uz.consortgroup.payment_service.exception.UnableToCancelException;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymeTransactionValidatorServiceImpl implements PaymeTransactionValidatorService {

    @Override
    public void validateTransactionState(PaymeTransaction tx, PaymeTransactionState requiredState) {
        log.info("Validating transaction state: required={}, actual={}, txId={}",
                requiredState, tx.getState(), tx.getPaycomTransactionId());

        if (tx.getState() != requiredState) {
            log.warn("Invalid transaction state: txId={}, expected={}, actual={}",
                    tx.getPaycomTransactionId(), requiredState, tx.getState());
            throw new TransactionNotFoundException("Transaction not found");
        }

        log.info("Transaction state is valid: txId={}", tx.getPaycomTransactionId());
    }

    @Override
    public void validateTransactionCancelable(PaymeTransaction tx) {
        log.info("Validating transaction cancelability: state={}, txId={}",
                tx.getState(), tx.getPaycomTransactionId());

        if (tx.getState() == PaymeTransactionState.CANCELED) {
            log.warn("Transaction already canceled: txId={}", tx.getPaycomTransactionId());
            throw new TransactionAlreadyCanceledException("Transaction already canceled");
        }

        if (tx.getState() == PaymeTransactionState.COMPLETED) {
            log.warn("Transaction already completed: txId={}", tx.getPaycomTransactionId());
            throw new UnableToCancelException("Transaction already completed");
        }

        log.info("Transaction is valid for cancellation: txId={}", tx.getPaycomTransactionId());
    }
}
