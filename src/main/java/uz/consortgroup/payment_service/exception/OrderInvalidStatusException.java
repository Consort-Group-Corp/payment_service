package uz.consortgroup.payment_service.exception;

public class OrderInvalidStatusException extends RuntimeException {
    public OrderInvalidStatusException(String message) {
        super(message);
    }
}