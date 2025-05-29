package uz.consortgroup.payment_service.validator;

import uz.consortgroup.payment_service.entity.PaymeTransaction;
import uz.consortgroup.payment_service.entity.PaymeTransactionState;

public interface PaymeTransactionValidatorService {
    void validateTransactionState(PaymeTransaction tx, PaymeTransactionState requiredState);
    void validateTransactionCancelable(PaymeTransaction tx);
}
