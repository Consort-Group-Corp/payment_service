package uz.consortgroup.payment_service.exception.click;

import java.util.Map;

public class AlreadyPaidException extends ClickException {
    public AlreadyPaidException() {
        super(-4, Map.of("ru", "Заказ уже оплачен", "en", "Order already paid"));
    }
}
