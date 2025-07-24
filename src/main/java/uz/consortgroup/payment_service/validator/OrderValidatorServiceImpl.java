package uz.consortgroup.payment_service.validator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderSource;
import uz.consortgroup.payment_service.entity.Order;
import uz.consortgroup.payment_service.exception.AmountMismatchException;
import uz.consortgroup.payment_service.exception.OrderInvalidStatusException;
import uz.consortgroup.payment_service.exception.OrderNotFoundException;
import uz.consortgroup.payment_service.repository.OrderRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderValidatorServiceImpl implements OrderValidatorService {
    private final OrderRepository orderRepository;

    @Override
    public Order validateOrderExists(String externalOrderId, OrderSource source) {
        log.info("Validating order existence: externalOrderId={}, source={}", externalOrderId, source);

        return orderRepository.findByExternalOrderIdAndSource(externalOrderId, source)
                .orElseThrow(() -> {
                    log.warn("Order not found: externalOrderId={}, source={}", externalOrderId, source);
                    return new OrderNotFoundException("Order not found");
                });
    }

    @Override
    public void validateAmount(Order order, Long amountInTiyin) {
        Long orderAmount = order.getAmount();
        log.info("Validating order amount: expected={}, actual={}", orderAmount, amountInTiyin);

        if (!orderAmount.equals(amountInTiyin)) {
            log.warn("Amount mismatch: orderId={}, expected={}, actual={}",
                    order.getExternalOrderId(), orderAmount, amountInTiyin);
            throw new AmountMismatchException("Transaction amount does not match order amount");
        }

        log.info("Order amount validated successfully: orderId={}", order.getExternalOrderId());
    }

    @Override
    public void validateOrderStatus(Order order) {
        log.info("Validating order status: orderId={}, status={}",
                order.getExternalOrderId(), order.getStatus());

        if (order.getStatus() == null || !order.getStatus().isPayable()) {
            log.warn("Invalid order status for payment: orderId={}, status={}",
                    order.getExternalOrderId(), order.getStatus());
            throw new OrderInvalidStatusException("Order status does not allow payment");
        }

        log.info("Order status is valid for payment: orderId={}", order.getExternalOrderId());
    }
}
