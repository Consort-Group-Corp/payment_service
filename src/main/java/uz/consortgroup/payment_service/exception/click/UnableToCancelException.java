package uz.consortgroup.payment_service.exception.click;

import java.util.Map;

public class UnableToCancelException extends ClickException {
    public UnableToCancelException() {
        super(-11, Map.of(
            "en", "Could not cancel transaction",
            "ru", "Невозможно отменить транзакцию"
        ));
    }
}
