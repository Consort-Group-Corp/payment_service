package uz.consortgroup.payment_service.exception;

public class AmountMismatchException extends RuntimeException {
    public AmountMismatchException(String message) {
        super(message);
    }
}