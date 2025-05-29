package uz.consortgroup.payment_service.exception.click;

import java.util.Map;

public class IncorrectAmountException extends ClickException {
    public IncorrectAmountException() {
        super(-2, Map.of("ru", "Неверная сумма", "en", "Incorrect amount"));
    }
}
