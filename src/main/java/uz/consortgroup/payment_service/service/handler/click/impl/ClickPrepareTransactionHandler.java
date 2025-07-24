package uz.consortgroup.payment_service.service.handler.click.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderSource;
import uz.consortgroup.payment_service.dto.click.ClickAction;
import uz.consortgroup.payment_service.dto.click.ClickError;
import uz.consortgroup.payment_service.dto.click.ClickRequest;
import uz.consortgroup.payment_service.dto.click.ClickResponse;
import uz.consortgroup.payment_service.entity.ClickTransaction;
import uz.consortgroup.payment_service.entity.ClickTransactionState;
import uz.consortgroup.payment_service.entity.Order;
import uz.consortgroup.payment_service.repository.ClickTransactionRepository;
import uz.consortgroup.payment_service.service.handler.click.ClickMethodHandler;
import uz.consortgroup.payment_service.validator.ClickTransactionValidatorService;
import uz.consortgroup.payment_service.validator.OrderValidatorService;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClickPrepareTransactionHandler implements ClickMethodHandler {
    private final ClickTransactionRepository clickTransactionRepository;
    private final OrderValidatorService orderValidator;
    private final ClickTransactionValidatorService txValidator;

    @Override
    public Integer getAction() {
        return ClickAction.CHECK.getCode(); // 0
    }

    @Override
    @Transactional
    public ClickResponse handle(ClickRequest req) {
        log.info("Start handling prepare transaction: clickTransactionId={}, merchantTransactionId={}",
                req.getClickTransactionId(), req.getMerchantTransactionId());

        txValidator.validateSignature(req);
        log.info("Signature successfully validated for clickTransactionId={}", req.getClickTransactionId());

        if (clickTransactionRepository.existsByClickTransactionId(req.getClickTransactionId())) {
            log.info("Transaction already exists: clickTransactionId={}", req.getClickTransactionId());
            ClickTransaction existing = clickTransactionRepository
                    .findByClickTransactionId(req.getClickTransactionId())
                    .orElseThrow(() -> {
                        log.warn("Transaction not found after existence check: clickTransactionId={}", req.getClickTransactionId());
                        return ClickError.TRANSACTION_NOT_FOUND.createException();
                    });

            if (existing.getState() == ClickTransactionState.COMPLETED) {
                log.warn("Transaction already completed: clickTransactionId={}", existing.getClickTransactionId());
                throw ClickError.ALREADY_PAID.createException();
            }

            return ClickResponse.success(
                    existing.getClickTransactionId(),
                    existing.getMerchantTransactionId(),
                    existing.getMerchantPrepareId()
            );
        }

        Order order = orderValidator.validateOrderExists(
                req.getMerchantTransactionId(),
                OrderSource.CLICK
        );
        log.info("Order found and validated: orderId={}", order.getId());

        orderValidator.validateAmount(order, req.getAmount());
        orderValidator.validateOrderStatus(order);

        String merchantPrepareId = UUID.randomUUID().toString();
        ClickTransaction tx = ClickTransaction.builder()
                .clickTransactionId(req.getClickTransactionId())
                .serviceId(req.getServiceId())
                .merchantTransactionId(req.getMerchantTransactionId())
                .merchantPrepareId(merchantPrepareId)
                .amount(req.getAmount())
                .action(req.getAction())
                .signTime(Instant.parse(req.getSignTime()))
                .signString(req.getSignString())
                .state(ClickTransactionState.CREATED)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        clickTransactionRepository.save(tx);
        log.info("New transaction created and saved: clickTransactionId={}, merchantPrepareId={}",
                tx.getClickTransactionId(), merchantPrepareId);

        return ClickResponse.success(
                tx.getClickTransactionId(),
                tx.getMerchantTransactionId(),
                merchantPrepareId
        );
    }
}
