package uz.consortgroup.payment_service.validator;

import uz.consortgroup.payment_service.dto.click.ClickRequest;
import uz.consortgroup.payment_service.entity.ClickTransaction;
import uz.consortgroup.payment_service.entity.ClickTransactionState;

public interface ClickTransactionValidatorService {
    void validateTransactionState(ClickTransaction tx, ClickTransactionState requiredState);
    void validateTransactionCancelable(ClickTransaction tx);
    void validateSignature(ClickRequest request);
}
