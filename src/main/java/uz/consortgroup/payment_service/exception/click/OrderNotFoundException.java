package uz.consortgroup.payment_service.exception.click;

import java.util.Map;

public class OrderNotFoundException extends ClickException {
    public OrderNotFoundException() {
        super(-6, Map.of("ru", "Заказ не найден", "en", "Order not found"));
    }
}
