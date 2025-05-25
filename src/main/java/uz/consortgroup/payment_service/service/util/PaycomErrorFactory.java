package uz.consortgroup.payment_service.service.util;

import uz.consortgroup.payment_service.dto.PaycomError;

import java.util.Map;

public class PaycomErrorFactory {

    public static PaycomError parseError() {
        return PaycomError.builder()
                .code(-32700)
                .message(Map.of("ru", "Ошибка парсинга JSON", "en", "Parse error"))
                .build();
    }

    public static PaycomError invalidRequest() {
        return PaycomError.builder()
                .code(-32600)
                .message(Map.of("ru", "Неверный формат запроса", "en", "Invalid Request"))
                .build();
    }

    public static PaycomError methodNotFound() {
        return PaycomError.builder()
                .code(-32601)
                .message(Map.of("ru", "Метод не найден", "en", "Method not found"))
                .build();
    }

    public static PaycomError internalError() {
        return PaycomError.builder()
                .code(-32603)
                .message(Map.of("ru", "Внутренняя ошибка", "en", "Internal error"))
                .build();
    }

    public static PaycomError orderNotFound() {
        return PaycomError.builder()
                .code(-31050)
                .message(Map.of("ru", "Заказ не найден", "en", "Order not found"))
                .data("order")
                .build();
    }

    public static PaycomError transactionNotFound() {
        return PaycomError.builder()
                .code(-31051)
                .message(Map.of("ru", "Транзакция не найдена", "en", "Transaction not found"))
                .data("transaction")
                .build();
    }

    public static PaycomError transactionAlreadyPerformed() {
        return PaycomError.builder()
                .code(-31052)
                .message(Map.of("ru", "Транзакция уже выполнена", "en", "Transaction already performed"))
                .data("transaction")
                .build();
    }

    public static PaycomError transactionCancelled() {
        return PaycomError.builder()
                .code(-31054)
                .message(Map.of("ru", "Транзакция отменена", "en", "Transaction cancelled"))
                .data("transaction")
                .build();
    }

    public static PaycomError transactionInvalidState() {
        return PaycomError.builder()
                .code(-31055)
                .message(Map.of("ru", "Транзакция в неверном состоянии", "en", "Transaction invalid state"))
                .data("transaction")
                .build();
    }

    public static PaycomError unableToCancel() {
        return PaycomError.builder()
                .code(-31007)
                .message(Map.of(
                        "ru", "Невозможно отменить транзакцию, так как она уже выполнена",
                        "en", "Cannot cancel transaction because it is already completed"
                ))
                .data("state")
                .build();
    }
}
