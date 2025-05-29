package uz.consortgroup.payment_service.exception.paycom;

import java.util.Map;

public class TransactionInvalidStateException extends PaycomException {
    public TransactionInvalidStateException() {
        super(-31055, Map.of("ru", "Транзакция в неверном состоянии", "en", "Transaction invalid state"), "transaction");
    }
}