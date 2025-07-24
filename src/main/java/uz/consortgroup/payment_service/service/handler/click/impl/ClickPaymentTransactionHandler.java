package uz.consortgroup.payment_service.service.handler.click.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderSource;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderStatus;
import uz.consortgroup.payment_service.dto.click.ClickAction;
import uz.consortgroup.payment_service.dto.click.ClickError;
import uz.consortgroup.payment_service.dto.click.ClickRequest;
import uz.consortgroup.payment_service.dto.click.ClickResponse;
import uz.consortgroup.payment_service.entity.ClickTransaction;
import uz.consortgroup.payment_service.entity.ClickTransactionState;
import uz.consortgroup.payment_service.entity.Order;
import uz.consortgroup.payment_service.repository.ClickTransactionRepository;
import uz.consortgroup.payment_service.repository.OrderRepository;
import uz.consortgroup.payment_service.service.handler.click.ClickMethodHandler;
import uz.consortgroup.payment_service.service.order.OrderEventPublisherStrategy;
import uz.consortgroup.payment_service.validator.ClickTransactionValidatorService;
import uz.consortgroup.payment_service.validator.OrderValidatorService;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClickPaymentTransactionHandler implements ClickMethodHandler {
    private final ClickTransactionRepository clickTransactionRepository;
    private final OrderValidatorService orderValidatorService;
    private final ClickTransactionValidatorService clickTransactionValidatorService;
    private final OrderRepository orderRepository;
    private final OrderEventPublisherStrategy orderEventPublisherStrategy;

    @Override
    public Integer getAction() {
        return ClickAction.PAYMENT.getCode(); // 1
    }

    @Override
    @Transactional
    public ClickResponse handle(ClickRequest request) {
        log.info("Start handling payment transaction: clickTransactionId={}, merchantTransactionId={}",
                request.getClickTransactionId(), request.getMerchantTransactionId());

        clickTransactionValidatorService.validateSignature(request);
        log.info("Signature successfully validated for clickTransactionId={}", request.getClickTransactionId());

        ClickTransaction transaction = clickTransactionRepository
                .findByClickTransactionId(request.getClickTransactionId())
                .orElseThrow(() -> {
                    log.warn("Transaction not found: clickTransactionId={}", request.getClickTransactionId());
                    return ClickError.TRANSACTION_NOT_FOUND.createException();
                });

        clickTransactionValidatorService.validateTransactionState(transaction, ClickTransactionState.CREATED);
        log.info("Transaction state is valid for payment: clickTransactionId={}", transaction.getClickTransactionId());

        if (request.getMerchantPrepareId() == null || !request.getMerchantPrepareId().equals(transaction.getMerchantPrepareId())) {
            log.warn("Invalid prepare ID: received={}, expected={}",
                    request.getMerchantPrepareId(), transaction.getMerchantPrepareId());
            throw ClickError.REQUEST_ERROR.createException();
        }

        Order order = orderValidatorService.validateOrderExists(
                request.getMerchantTransactionId(),
                OrderSource.CLICK
        );
        log.info("Order found and validated: orderId={}", order.getId());

        orderValidatorService.validateAmount(order, request.getAmount());
        orderValidatorService.validateOrderStatus(order);

        transaction.setState(ClickTransactionState.COMPLETED);
        transaction.setPerformTime(Instant.now());
        transaction.setUpdatedAt(Instant.now());
        clickTransactionRepository.save(transaction);
        log.info("Transaction marked as COMPLETED: clickTransactionId={}", transaction.getClickTransactionId());

        orderEventPublisherStrategy.sendEvent(order);
        log.info("Order event published: orderId={}", order.getId());

        order.setStatus(OrderStatus.PAID);
        order.setUpdatedAt(Instant.now());
        orderRepository.save(order);
        log.info("Order status updated to PAID: orderId={}", order.getId());

        return ClickResponse.success(
                transaction.getClickTransactionId(),
                transaction.getMerchantTransactionId(),
                transaction.getMerchantPrepareId()
        );
    }
}
