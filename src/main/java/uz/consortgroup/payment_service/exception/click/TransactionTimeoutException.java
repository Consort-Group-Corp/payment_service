package uz.consortgroup.payment_service.exception.click;

import java.util.Map;

public class TransactionTimeoutException extends ClickException {
    public TransactionTimeoutException() {
        super(-12, Map.of(
            "en", "Transaction timeout",
            "ru", "Тайм-аут транзакции"
        ));
    }
}
