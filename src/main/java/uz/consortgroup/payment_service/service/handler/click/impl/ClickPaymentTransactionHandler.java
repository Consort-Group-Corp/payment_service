package uz.consortgroup.payment_service.service.handler.click.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderSource;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderStatus;
import uz.consortgroup.payment_service.asspect.annotation.AllAspect;
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
    @AllAspect
    @Transactional
    public ClickResponse handle(ClickRequest request) {
        clickTransactionValidatorService.validateSignature(request);

        ClickTransaction transaction = clickTransactionRepository
                .findByClickTransactionId(request.getClickTransactionId())
                .orElseThrow(ClickError.TRANSACTION_NOT_FOUND::createException);

        clickTransactionValidatorService.validateTransactionState(transaction, ClickTransactionState.CREATED);

        if (request.getMerchantPrepareId() == null || !request.getMerchantPrepareId().equals(transaction.getMerchantPrepareId())) {
            throw ClickError.REQUEST_ERROR.createException();
        }

        Order order = orderValidatorService.validateOrderExists(
                request.getMerchantTransactionId(),
                OrderSource.CLICK
        );
        orderValidatorService.validateAmount(order, request.getAmount());
        orderValidatorService.validateOrderStatus(order);

        transaction.setState(ClickTransactionState.COMPLETED);
        transaction.setPerformTime(Instant.now());
        transaction.setUpdatedAt(Instant.now());
        clickTransactionRepository.save(transaction);

        orderEventPublisherStrategy.sendEvent(order);

        order.setStatus(OrderStatus.PAID);
        order.setUpdatedAt(Instant.now());
        orderRepository.save(order);

        return ClickResponse.success(
                transaction.getClickTransactionId(),
                transaction.getMerchantTransactionId(),
                transaction.getMerchantPrepareId()
        );
    }
}