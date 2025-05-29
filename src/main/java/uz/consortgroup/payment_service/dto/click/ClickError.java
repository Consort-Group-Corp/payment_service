package uz.consortgroup.payment_service.dto.click;

import lombok.Getter;
import uz.consortgroup.payment_service.exception.click.*;

import java.util.function.Supplier;

@Getter
public enum ClickError {
    SUCCESS(0, "Success", () -> null),
    SIGNATURE_ERROR(-1, "SIGN CHECK FAILED!", SignatureErrorException::new),
    INCORRECT_AMOUNT(-2, "Incorrect amount", IncorrectAmountException::new),
    INVALID_ACTION(-3, "Action is invalid", InvalidActionException::new),
    ALREADY_PAID(-4, "Order already paid", AlreadyPaidException::new),
    TRANSACTION_NOT_FOUND(-5, "Transaction not found", TransactionNotFoundException::new),
    ORDER_NOT_FOUND(-6, "Order not found", OrderNotFoundException::new),
    TRANSACTION_CANCELLED(-7, "Transaction canceled", TransactionAlreadyCancelledException::new),
    REQUEST_ERROR(-8, "Error in request from merchant", InvalidRequestException::new),
    TRANSACTION_NOT_CONFIRMED(-9, "Transaction is not confirmed", TransactionNotConfirmedException::new),
    INCORRECT_REQUEST(-10, "Incorrect request", IncorrectRequestException::new),
    CANNOT_CANCEL(-11, "Could not cancel transaction", UnableToCancelException::new),
    TRANSACTION_TIMEOUT(-12, "Transaction timeout", TransactionTimeoutException::new),
    TRANSACTION_NOT_FOUND_ALT(-13, "Transaction not found", TransactionNotFoundException::new),
    UNKNOWN_ERROR(-999, "Unknown error", UnknownException::new);

    private final int code;
    private final String note;
    private final Supplier<ClickException> exceptionSupplier;

    ClickError(int code, String note, Supplier<ClickException> exceptionSupplier) {
        this.code = code;
        this.note = note;
        this.exceptionSupplier = exceptionSupplier;
    }

    public ClickException createException() {
        return exceptionSupplier.get();
    }
}
