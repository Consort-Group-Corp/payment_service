package uz.consortgroup.payment_service.exception;

import java.util.Map;

public class TransactionCancelledException extends PaycomException {
    public TransactionCancelledException() {
        super(-31054, Map.of("ru", "Транзакция отменена", "en", "Transaction cancelled"), "transaction");
    }
}