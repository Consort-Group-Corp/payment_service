package uz.consortgroup.payment_service.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.consortgroup.payment_service.asspect.annotation.AllAspect;
import uz.consortgroup.payment_service.asspect.annotation.AspectAfterThrowing;
import uz.consortgroup.payment_service.asspect.annotation.LoggingAspectAfterMethod;
import uz.consortgroup.payment_service.asspect.annotation.LoggingAspectBeforeMethod;
import uz.consortgroup.payment_service.entity.Order;
import uz.consortgroup.payment_service.entity.OrderSource;
import uz.consortgroup.payment_service.entity.PaymeTransaction;
import uz.consortgroup.payment_service.entity.PaymeTransactionState;
import uz.consortgroup.payment_service.exception.AmountMismatchException;
import uz.consortgroup.payment_service.exception.OrderInvalidStatusException;
import uz.consortgroup.payment_service.exception.OrderNotFoundException;
import uz.consortgroup.payment_service.exception.TransactionAlreadyCanceledException;
import uz.consortgroup.payment_service.exception.TransactionNotFoundException;
import uz.consortgroup.payment_service.exception.UnableToCancelException;
import uz.consortgroup.payment_service.repository.OrderRepository;

@Service
@RequiredArgsConstructor
public class OrderValidatorServiceImpl implements OrderValidatorService {
    private final OrderRepository orderRepository;

    @AllAspect
    public Order validateOrderExists(String externalOrderId, OrderSource source) {
        return orderRepository.findByExternalOrderIdAndSource(externalOrderId, source)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));
    }

    @LoggingAspectBeforeMethod
    @LoggingAspectAfterMethod
    @AspectAfterThrowing
    public void validateAmount(Order order, Long amountInTiyin) {
        Long orderAmount = order.getAmount();
        if (!orderAmount.equals(amountInTiyin)) {
            throw new AmountMismatchException("Transaction amount does not match order amount");
        }
    }

    @LoggingAspectBeforeMethod
    @LoggingAspectAfterMethod
    @AspectAfterThrowing
    public void validateOrderStatus(Order order) {
        if (order.getStatus() == null || !order.getStatus().isPayable()) {
            throw new OrderInvalidStatusException("Order status does not allow payment");
        }
    }

    @LoggingAspectBeforeMethod
    @LoggingAspectAfterMethod
    @AspectAfterThrowing
    public void validateTransactionState(PaymeTransaction tx, PaymeTransactionState requiredState) {
        if (tx.getState() != requiredState) {
            throw new TransactionNotFoundException("Transaction not found");
        }
    }

    @LoggingAspectBeforeMethod
    @LoggingAspectAfterMethod
    @AspectAfterThrowing
    public void validateTransactionCancelable(PaymeTransaction tx) {
        if (tx.getState() == PaymeTransactionState.CANCELED) {
            throw new TransactionAlreadyCanceledException("Transaction already canceled");
        }
        if (tx.getState() == PaymeTransactionState.COMPLETED) {
            throw new UnableToCancelException("Transaction already completed");
        }
    }
}
