package uz.consortgroup.payment_service.exception;

import java.util.Map;

public class TransactionAlreadyPerformedException extends PaycomException {
    public TransactionAlreadyPerformedException() {
        super(-31052, Map.of("ru", "Транзакция уже выполнена", "en", "Transaction already performed"), "transaction");
    }
}