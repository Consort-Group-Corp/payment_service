package uz.consortgroup.payment_service.exception.click;

import java.util.Map;

public class TransactionNotFoundException extends ClickException {
    public TransactionNotFoundException() {
        super(-5, Map.of("ru", "Транзакция не найдена", "en", "Transaction not found"));
    }
}
