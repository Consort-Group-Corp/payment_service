package uz.consortgroup.payment_service.validator;

import uz.consortgroup.payment_service.entity.Order;
import uz.consortgroup.payment_service.entity.OrderSource;
import uz.consortgroup.payment_service.entity.PaymeTransaction;
import uz.consortgroup.payment_service.entity.PaymeTransactionState;

public interface OrderValidatorService {
    Order validateOrderExists(String externalOrderId, OrderSource source);
    void validateAmount(Order order, Long amountInTiyin);
    void validateOrderStatus(Order order);
    void validateTransactionState(PaymeTransaction tx, PaymeTransactionState requiredState);
    void validateTransactionCancelable(PaymeTransaction tx);
}
