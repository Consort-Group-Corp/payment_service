package uz.consortgroup.payment_service.exception.paycom;

import java.util.Map;

public class MethodNotFoundException extends PaycomException {
    public MethodNotFoundException() {
        super(-32601, Map.of("ru", "Метод не найден", "en", "Method not found"));
    }
}