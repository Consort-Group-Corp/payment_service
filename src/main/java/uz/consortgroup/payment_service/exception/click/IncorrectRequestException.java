package uz.consortgroup.payment_service.exception.click;

import java.util.Map;

public class IncorrectRequestException extends ClickException {
    public IncorrectRequestException() {
        super(-10, Map.of(
            "en", "Incorrect request",
            "ru", "Неверный запрос"
        ));
    }
}
