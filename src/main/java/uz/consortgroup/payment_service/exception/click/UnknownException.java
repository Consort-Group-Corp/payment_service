package uz.consortgroup.payment_service.exception.click;

import java.util.Map;

public class UnknownException extends ClickException {
    public UnknownException() {
        super(-999, Map.of(
                "ru", "Неизвестная ошибка",
                "en", "Unknown error"
        ));
    }
}

