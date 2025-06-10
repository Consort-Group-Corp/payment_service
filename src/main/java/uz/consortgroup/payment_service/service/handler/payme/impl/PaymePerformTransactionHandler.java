package uz.consortgroup.payment_service.service.handler.payme.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderSource;
import uz.consortgroup.payment_service.asspect.annotation.AllAspect;
import uz.consortgroup.payment_service.dto.paycom.PaycomRequest;
import uz.consortgroup.payment_service.dto.paycom.PaycomResponse;
import uz.consortgroup.payment_service.dto.paycom.PerformTransactionParams;
import uz.consortgroup.payment_service.entity.PaymeTransaction;
import uz.consortgroup.payment_service.entity.PaymeTransactionState;
import uz.consortgroup.payment_service.exception.paycom.TransactionNotFoundException;
import uz.consortgroup.payment_service.repository.PaymeTransactionRepository;
import uz.consortgroup.payment_service.service.handler.payme.PaycomMethodHandler;
import uz.consortgroup.payment_service.service.order.OrderService;
import uz.consortgroup.payment_service.validator.PaymeTransactionValidatorService;

import java.time.Instant;
import java.util.Map;

import static uz.consortgroup.payment_service.service.util.JsonUtil.convertParams;

@Service
@RequiredArgsConstructor
public class PaymePerformTransactionHandler implements PaycomMethodHandler {
    private final PaymeTransactionRepository paymeTransactionRepository;
    private final PaymeTransactionValidatorService paymeTransactionValidatorService;
    private final OrderService orderService;

    @Override
    public String getMethod() {
        return "PerformTransaction";
    }

    @Override
    @Transactional
    @AllAspect
    public PaycomResponse handle(PaycomRequest request) {
        Object id = request.getId();
        PerformTransactionParams params = convertParams(request.getParams(), PerformTransactionParams.class);
        String paycomTransactionId = params.getId();

        PaymeTransaction tx = paymeTransactionRepository.findByPaycomTransactionId(paycomTransactionId)
                .orElseThrow(TransactionNotFoundException::new);

        if (tx.getState() == PaymeTransactionState.COMPLETED) {
            return PaycomResponse.success(id, buildResponse(tx));
        }

        paymeTransactionValidatorService.validateTransactionState(tx, PaymeTransactionState.CREATED);

        orderService.markAsPaidAndPublish(tx.getOrderId(), OrderSource.PAYME);

        tx.setState(PaymeTransactionState.COMPLETED);
        tx.setPerformTime(Instant.now());
        paymeTransactionRepository.save(tx);


        return PaycomResponse.success(id, buildResponse(tx));
    }

    private Map<String, Object> buildResponse(PaymeTransaction tx) {
        return Map.of(
                "perform_time", tx.getPerformTime() != null ? tx.getPerformTime().toEpochMilli() : null,
                "transaction", tx.getId().toString(),
                "state", tx.getState().getCode()
        );
    }
}
