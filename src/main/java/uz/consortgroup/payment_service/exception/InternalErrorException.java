package uz.consortgroup.payment_service.exception;

import java.util.Map;

public class InternalErrorException extends PaycomException {
    public InternalErrorException() {
        super(-32603, Map.of("ru", "Внутренняя ошибка", "en", "Internal error"));
    }
}