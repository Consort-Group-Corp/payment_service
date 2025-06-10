package uz.consortgroup.payment_service.validator;

import uz.consortgroup.core.api.v1.dto.payment.order.OrderSource;
import uz.consortgroup.payment_service.entity.Order;

public interface OrderValidatorService {
    Order validateOrderExists(String externalOrderId, OrderSource source);
    void validateAmount(Order order, Long amountInTiyin);
    void validateOrderStatus(Order order);
}
