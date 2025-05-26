package uz.consortgroup.payment_service.exception;

public class UnableToCancelException extends RuntimeException {
    public UnableToCancelException(String message) {
        super(message);
    }
}
