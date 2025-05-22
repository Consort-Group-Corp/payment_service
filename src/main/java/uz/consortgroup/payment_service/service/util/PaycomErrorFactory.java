package uz.consortgroup.payment_service.service.util;

import uz.consortgroup.payment_service.dto.PaycomError;

import java.util.Map;

public class PaycomErrorFactory {
    public static PaycomError methodNotFound() {
        Map<String, String> messageMap = Map.of("en", "Method not found", "ru", "Метод не найден");
        return PaycomError.builder()
                .code(-32601)
                .message(messageMap)
                .build();
    }

    public static PaycomError internalError() {
        Map<String, String> messageMap = Map.of("en", "Internal error", "ru", "Внутренняя ошибка");
        return PaycomError.builder()
                .code(-32603)
                .message(messageMap)
                .build();
    }
}
