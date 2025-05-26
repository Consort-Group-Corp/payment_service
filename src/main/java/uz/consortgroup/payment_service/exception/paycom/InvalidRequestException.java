package uz.consortgroup.payment_service.exception.paycom;

import java.util.Map;

public class InvalidRequestException extends PaycomException {
    public InvalidRequestException() {
        super(-32600, Map.of("ru", "Неверный формат запроса", "en", "Invalid Request"));
    }
}