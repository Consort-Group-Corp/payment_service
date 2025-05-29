package uz.consortgroup.payment_service.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.consortgroup.payment_service.asspect.annotation.AspectAfterThrowing;
import uz.consortgroup.payment_service.asspect.annotation.LoggingAspectAfterMethod;
import uz.consortgroup.payment_service.asspect.annotation.LoggingAspectBeforeMethod;
import uz.consortgroup.payment_service.entity.PaymeTransaction;
import uz.consortgroup.payment_service.entity.PaymeTransactionState;
import uz.consortgroup.payment_service.exception.TransactionAlreadyCanceledException;
import uz.consortgroup.payment_service.exception.TransactionNotFoundException;
import uz.consortgroup.payment_service.exception.UnableToCancelException;

@Service
@RequiredArgsConstructor
public class PaymeTransactionValidatorServiceImpl implements PaymeTransactionValidatorService {

    @LoggingAspectBeforeMethod
    @LoggingAspectAfterMethod
    @AspectAfterThrowing
    public void validateTransactionState(PaymeTransaction tx, PaymeTransactionState requiredState) {
        if (tx.getState() != requiredState) {
            throw new TransactionNotFoundException("Transaction not found");
        }
    }

    @LoggingAspectBeforeMethod
    @LoggingAspectAfterMethod
    @AspectAfterThrowing
    public void validateTransactionCancelable(PaymeTransaction tx) {
        if (tx.getState() == PaymeTransactionState.CANCELED) {
            throw new TransactionAlreadyCanceledException("Transaction already canceled");
        }
        if (tx.getState() == PaymeTransactionState.COMPLETED) {
            throw new UnableToCancelException("Transaction already completed");
        }
    }
}
