package uz.consortgroup.payment_service.service.handler.click.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.payment_service.asspect.annotation.AllAspect;
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
    @AllAspect
    @Transactional
    public ClickResponse handle(ClickRequest request) {
        clickTransactionValidatorService.validateSignature(request);

        ClickTransaction transaction = clickTransactionRepository
                .findByClickTransactionId(request.getClickTransactionId())
                .orElseThrow(ClickError.TRANSACTION_NOT_FOUND::createException);

        clickTransactionValidatorService.validateTransactionCancelable(transaction);

        transaction.setState(ClickTransactionState.CANCELED);
        transaction.setCancelTime(Instant.now());
        transaction.setCancelReason("Canceled by request");
        transaction.setUpdatedAt(Instant.now());
        clickTransactionRepository.save(transaction);

        return ClickResponse.success(
                transaction.getClickTransactionId(),
                transaction.getMerchantTransactionId(),
                transaction.getMerchantPrepareId()
        );
    }
}