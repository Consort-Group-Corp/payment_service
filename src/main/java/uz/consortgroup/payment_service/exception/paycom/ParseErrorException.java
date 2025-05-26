package uz.consortgroup.payment_service.exception.paycom;

import java.util.Map;

public class ParseErrorException extends PaycomException {
    public ParseErrorException() {
        super(-32700, Map.of("ru", "Ошибка парсинга JSON", "en", "Parse error"));
    }
}