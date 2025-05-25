package uz.consortgroup.payment_service.exception;

import java.util.Map;

public class OrderNotFoundException extends PaycomException {
    public OrderNotFoundException() {
        super(-31050, Map.of("ru", "Заказ не найден", "en", "Order not found"), "order");
    }
}