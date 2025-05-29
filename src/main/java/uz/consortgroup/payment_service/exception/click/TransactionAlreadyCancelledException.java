package uz.consortgroup.payment_service.exception.click;

import java.util.Map;

public class TransactionAlreadyCancelledException extends ClickException {
    public TransactionAlreadyCancelledException() {
        super(-7, Map.of(
            "en", "Transaction already cancelled",
            "ru", "Транзакция уже отменена"
        ));
    }
}
