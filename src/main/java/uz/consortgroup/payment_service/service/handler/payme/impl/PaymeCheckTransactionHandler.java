package uz.consortgroup.payment_service.service.handler.payme.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.payment_service.asspect.annotation.AllAspect;
import uz.consortgroup.payment_service.dto.paycom.CheckTransactionDto;
import uz.consortgroup.payment_service.dto.paycom.PaycomRequest;
import uz.consortgroup.payment_service.dto.paycom.PaycomResponse;
import uz.consortgroup.payment_service.entity.PaymeTransaction;
import uz.consortgroup.payment_service.exception.paycom.TransactionNotFoundException;
import uz.consortgroup.payment_service.repository.PaymeTransactionRepository;
import uz.consortgroup.payment_service.service.handler.payme.PaycomMethodHandler;

@Service
@RequiredArgsConstructor
public class PaymeCheckTransactionHandler implements PaycomMethodHandler {

    private final PaymeTransactionRepository paymeTransactionRepository;

    @Override
    public String getMethod() {
        return "CheckTransaction";
    }

    @Override
    @Transactional(readOnly = true)
    @AllAspect
    public PaycomResponse handle(PaycomRequest request) {
        Object id = request.getId();

        String paycomTransactionId = (String) request.getParams().get("id");

        PaymeTransaction tx = paymeTransactionRepository.findByPaycomTransactionId(paycomTransactionId)
                .orElseThrow(TransactionNotFoundException::new);

        CheckTransactionDto result = CheckTransactionDto.builder()
                .create_time(tx.getCreateTime().toEpochMilli())
                .perform_time(tx.getPerformTime() != null ? tx.getPerformTime().toEpochMilli() : null)
                .cancel_time(tx.getCancelTime() != null ? tx.getCancelTime().toEpochMilli() : null)
                .transaction(tx.getPaycomTransactionId())
                .state(tx.getState().getCode())
                .reason(tx.getReason())
                .build();

        return PaycomResponse.success(id, result);
    }
}
