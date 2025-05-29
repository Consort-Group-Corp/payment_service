package uz.consortgroup.payment_service.exception.click;

import java.util.Map;

public class InvalidRequestException extends ClickException {
    public InvalidRequestException() {
        super(-8, Map.of(
            "en", "Error in request from merchant",
            "ru", "Ошибка в запросе от мерчанта"
        ));
    }
}
