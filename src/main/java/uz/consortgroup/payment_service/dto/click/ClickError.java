package uz.consortgroup.payment_service.dto.click;

import lombok.Getter;
import uz.consortgroup.payment_service.exception.click.AlreadyPaidException;
import uz.consortgroup.payment_service.exception.click.ClickException;
import uz.consortgroup.payment_service.exception.click.IncorrectAmountException;
import uz.consortgroup.payment_service.exception.click.InvalidActionException;
import uz.consortgroup.payment_service.exception.click.OrderNotFoundException;
import uz.consortgroup.payment_service.exception.click.SignatureErrorException;
import uz.consortgroup.payment_service.exception.click.TransactionNotFoundException;
import uz.consortgroup.payment_service.exception.click.UnknownException;

import java.util.function.Supplier;

public enum ClickError {
    SUCCESS(0, "Success", () -> null),
    SIGNATURE_ERROR(-1, "SIGN CHECK FAILED!", SignatureErrorException::new),
    TRANSACTION_NOT_FOUND(-5, "Transaction not found", TransactionNotFoundException::new),
    ORDER_NOT_FOUND(-6, "Order not found", OrderNotFoundException::new),
    INCORRECT_AMOUNT(-2, "Incorrect amount", IncorrectAmountException::new),
    INVALID_ACTION(-3, "Action is invalid", InvalidActionException::new),
    ALREADY_PAID(-4, "Order already paid", AlreadyPaidException::new),
    UNKNOWN_ERROR(-999, "Unknown error", UnknownException::new);

    @Getter
    private final int code;
    @Getter
    private final String note;
    private final Supplier<ClickException> exceptionSupplier;

    ClickError(int code, String note, Supplier<ClickException> exceptionSupplier) {
        this.code = code;
        this.note = note;
        this.exceptionSupplier = exceptionSupplier;
    }

    public ClickException createException() { return exceptionSupplier.get(); }
}
