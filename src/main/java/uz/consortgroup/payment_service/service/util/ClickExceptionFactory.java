package uz.consortgroup.payment_service.service.util;

import uz.consortgroup.payment_service.exception.click.AlreadyPaidException;
import uz.consortgroup.payment_service.exception.click.ClickException;
import uz.consortgroup.payment_service.exception.click.IncorrectAmountException;
import uz.consortgroup.payment_service.exception.click.InvalidActionException;
import uz.consortgroup.payment_service.exception.click.OrderNotFoundException;
import uz.consortgroup.payment_service.exception.click.SignatureErrorException;
import uz.consortgroup.payment_service.exception.click.TransactionNotFoundException;
import uz.consortgroup.payment_service.exception.click.UnknownException;

import java.util.Map;

public class ClickExceptionFactory {

    public static ClickException fromCode(int code) {
        return switch (code) {
            case -1 -> new SignatureErrorException();
            case -2 -> new IncorrectAmountException();
            case -3 -> new InvalidActionException();
            case -4 -> new AlreadyPaidException();
            case -5 -> new TransactionNotFoundException();
            case -6 -> new OrderNotFoundException();
            default -> new UnknownException();
        };
    }
}

