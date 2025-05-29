package uz.consortgroup.payment_service.exception.click;

import java.util.Map;

public class InvalidActionException extends ClickException {
    public InvalidActionException() {
        super(-3, Map.of("ru", "Неверное действие", "en", "Action is invalid"));
    }
}
