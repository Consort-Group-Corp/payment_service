package uz.consortgroup.payment_service.exception;

public class TransactionAlreadyCanceledException extends RuntimeException {
    public TransactionAlreadyCanceledException(String message) {
        super(message);
    }
}