package uz.consortgroup.payment_service.validator;

import uz.consortgroup.payment_service.entity.Order;
import uz.consortgroup.payment_service.entity.OrderSource;

public interface OrderValidatorService {
    Order validateOrderExists(String externalOrderId, OrderSource source);
    void validateAmount(Order order, Long amountInTiyin);
    void validateOrderStatus(Order order);
}
