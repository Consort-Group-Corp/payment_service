package uz.consortgroup.payment_service.service.handler.click.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.payment_service.dto.click.ClickAction;
import uz.consortgroup.payment_service.dto.click.ClickError;
import uz.consortgroup.payment_service.dto.click.ClickRequest;
import uz.consortgroup.payment_service.dto.click.ClickResponse;
import uz.consortgroup.payment_service.entity.ClickTransaction;
import uz.consortgroup.payment_service.entity.ClickTransactionState;
import uz.consortgroup.payment_service.repository.ClickTransactionRepository;
import uz.consortgroup.payment_service.service.handler.click.ClickMethodHandler;
import uz.consortgroup.payment_service.validator.ClickTransactionValidatorService;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClickCancelTransactionHandler implements ClickMethodHandler {
    private final ClickTransactionRepository clickTransactionRepository;
    private final ClickTransactionValidatorService clickTransactionValidatorService;

    @Override
    public Integer getAction() {
        return ClickAction.CANCEL.getCode(); // 2
    }

    @Override
    @Transactional
    public ClickResponse handle(ClickRequest request) {
        log.info("Start handling cancel transaction request: clickTransactionId={}, merchantTransactionId={}",
                request.getClickTransactionId(), request.getMerchantTransactionId());

        clickTransactionValidatorService.validateSignature(request);
        log.info("Signature successfully validated for clickTransactionId={}", request.getClickTransactionId());

        ClickTransaction transaction = clickTransactionRepository
                .findByClickTransactionId(request.getClickTransactionId())
                .orElseThrow(() -> {
                    log.warn("Transaction not found: clickTransactionId={}", request.getClickTransactionId());
                    return ClickError.TRANSACTION_NOT_FOUND.createException();
                });

        clickTransactionValidatorService.validateTransactionCancelable(transaction);
        log.info("Transaction is valid for cancellation: clickTransactionId={}", transaction.getClickTransactionId());

        transaction.setState(ClickTransactionState.CANCELED);
        transaction.setCancelTime(Instant.now());
        transaction.setCancelReason("Canceled by request");
        transaction.setUpdatedAt(Instant.now());
        clickTransactionRepository.save(transaction);
        log.info("Transaction canceled successfully: clickTransactionId={}", transaction.getClickTransactionId());

        return ClickResponse.success(
                transaction.getClickTransactionId(),
                transaction.getMerchantTransactionId(),
                transaction.getMerchantPrepareId()
        );
    }
}
