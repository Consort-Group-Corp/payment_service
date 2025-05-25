package uz.consortgroup.payment_service.exception;

import java.util.Map;

public class TransactionNotFoundException extends PaycomException {
    public TransactionNotFoundException() {
        super(-31051, Map.of("ru", "Транзакция не найдена", "en", "Transaction not found"), "transaction");
    }
}