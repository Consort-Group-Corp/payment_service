package uz.consortgroup.payment_service.exception.paycom;

import java.util.Map;

public class UnableToCancelException extends PaycomException {
    public UnableToCancelException() {
        super(-31008, Map.of(
                "ru", "Невозможно отменить транзакцию, так как она уже выполнена",
                "en", "Cannot cancel transaction because it is already completed"
        ), "state");
    }
}