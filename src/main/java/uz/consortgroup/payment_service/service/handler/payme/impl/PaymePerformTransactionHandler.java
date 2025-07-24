package uz.consortgroup.payment_service.service.handler.payme.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderSource;
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

@Slf4j
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
    public PaycomResponse handle(PaycomRequest request) {
        Object id = request.getId();
        log.info("Handling PerformTransaction request: requestId={}", id);

        PerformTransactionParams params = convertParams(request.getParams(), PerformTransactionParams.class);
        String paycomTransactionId = params.getId();

        PaymeTransaction tx = paymeTransactionRepository.findByPaycomTransactionId(paycomTransactionId)
                .orElseThrow(() -> {
                    log.warn("Transaction not found: paycomTransactionId={}", paycomTransactionId);
                    return new TransactionNotFoundException();
                });

        if (tx.getState() == PaymeTransactionState.COMPLETED) {
            log.info("Transaction already completed: transactionId={}", tx.getId());
            return PaycomResponse.success(id, buildResponse(tx));
        }

        paymeTransactionValidatorService.validateTransactionState(tx, PaymeTransactionState.CREATED);
        log.info("Transaction is valid for completion: transactionId={}", tx.getId());

        orderService.markAsPaidAndPublish(tx.getOrderId(), OrderSource.PAYME);
        log.info("Order marked as paid and event published: orderId={}", tx.getOrderId());

        tx.setState(PaymeTransactionState.COMPLETED);
        tx.setPerformTime(Instant.now());
        paymeTransactionRepository.save(tx);

        log.info("Transaction marked as COMPLETED: transactionId={}, performTime={}",
                tx.getId(), tx.getPerformTime());

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
