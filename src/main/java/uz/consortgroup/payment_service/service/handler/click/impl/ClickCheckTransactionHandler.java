package uz.consortgroup.payment_service.service.handler.click.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.consortgroup.payment_service.asspect.annotation.AllAspect;
import uz.consortgroup.payment_service.dto.click.ClickAction;
import uz.consortgroup.payment_service.dto.click.ClickRequest;
import uz.consortgroup.payment_service.dto.click.ClickResponse;
import uz.consortgroup.payment_service.entity.ClickTransaction;
import uz.consortgroup.payment_service.exception.click.TransactionNotFoundException;
import uz.consortgroup.payment_service.repository.ClickTransactionRepository;
import uz.consortgroup.payment_service.service.handler.click.ClickMethodHandler;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClickCheckTransactionHandler implements ClickMethodHandler {
    private final ClickTransactionRepository clickTransactionRepository;

    @Override
    public Integer getAction() {
        return ClickAction.CHECK.getCode();
    }

    @Override
    @AllAspect
    public ClickResponse handle(ClickRequest request) {
        Long transactionId = request.getClickTransactionId();

        Optional<ClickTransaction> transactionOpt = clickTransactionRepository.findByClickTransactionId(transactionId);

        if (transactionOpt.isEmpty()) {
            throw new TransactionNotFoundException();
        }

        ClickTransaction transaction = transactionOpt.get();

        return ClickResponse.success(
                transaction.getClickTransactionId(),
                transaction.getMerchantTransactionId(),
                transaction.getMerchantPrepareId() != null ? transaction.getMerchantPrepareId().toString() : null
        );
    }
}
