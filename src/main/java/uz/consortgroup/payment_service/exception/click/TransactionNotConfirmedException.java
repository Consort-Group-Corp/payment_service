package uz.consortgroup.payment_service.exception.click;

import java.util.Map;

public class TransactionNotConfirmedException extends ClickException {
    public TransactionNotConfirmedException() {
        super(-9, Map.of(
            "en", "Transaction is not confirmed",
            "ru", "Транзакция не подтверждена"
        ));
    }
}
