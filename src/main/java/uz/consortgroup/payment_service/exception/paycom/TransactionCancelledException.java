package uz.consortgroup.payment_service.exception.paycom;

import java.util.Map;

public class TransactionCancelledException extends PaycomException {
    public TransactionCancelledException() {
        super(-31054, Map.of("ru", "Транзакция отменена", "en", "Transaction cancelled"), "transaction");
    }
}