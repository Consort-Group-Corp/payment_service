package uz.consortgroup.payment_service.service.handler.payme.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderSource;
import uz.consortgroup.payment_service.dto.paycom.CreateTransactionParams;
import uz.consortgroup.payment_service.dto.paycom.PaycomRequest;
import uz.consortgroup.payment_service.dto.paycom.PaycomResponse;
import uz.consortgroup.payment_service.entity.Order;
import uz.consortgroup.payment_service.entity.PaymeTransaction;
import uz.consortgroup.payment_service.entity.PaymeTransactionState;
import uz.consortgroup.payment_service.repository.PaymeTransactionRepository;
import uz.consortgroup.payment_service.service.handler.payme.PaycomMethodHandler;
import uz.consortgroup.payment_service.validator.OrderValidatorService;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static uz.consortgroup.payment_service.service.util.JsonUtil.convertParams;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymeCreateTransactionHandler implements PaycomMethodHandler {

    private final PaymeTransactionRepository paymeTransactionRepository;
    private final OrderValidatorService orderValidatorService;

    @Override
    public String getMethod() {
        return "CreateTransaction";
    }

    @Override
    @Transactional
    public PaycomResponse handle(PaycomRequest request) {
        Object id = request.getId();
        log.info("Handling CreateTransaction request: requestId={}", id);

        CreateTransactionParams params = convertParams(request.getParams(), CreateTransactionParams.class);
        String paycomTransactionId = params.getId();

        Optional<PaymeTransaction> existing = paymeTransactionRepository.findByPaycomTransactionId(paycomTransactionId);
        if (existing.isPresent()) {
            log.info("Transaction already exists: paycomTransactionId={}", paycomTransactionId);
            return PaycomResponse.success(id, Map.of(
                    "create_time", existing.get().getCreateTime().toEpochMilli(),
                    "transaction", existing.get().getId().toString(),
                    "state", existing.get().getState().getCode()
            ));
        }

        String orderId = params.getAccount().getOrderId();
        Long amount = params.getAmount();

        log.info("Validating order: orderId={}, amount={}", orderId, amount);
        Order order = orderValidatorService.validateOrderExists(orderId, OrderSource.PAYME);
        orderValidatorService.validateAmount(order, amount);
        orderValidatorService.validateOrderStatus(order);

        PaymeTransaction paymeTransaction = PaymeTransaction.builder()
                .paycomTransactionId(paycomTransactionId)
                .orderId(orderId)
                .amount(amount)
                .state(PaymeTransactionState.CREATED)
                .createTime(Instant.now())
                .build();

        paymeTransactionRepository.save(paymeTransaction);
        log.info("Transaction successfully created: id={}, orderId={}", paymeTransaction.getId(), orderId);

        Map<String, Object> result = Map.of(
                "create_time", paymeTransaction.getCreateTime().toEpochMilli(),
                "transaction", paymeTransaction.getId().toString(),
                "state", paymeTransaction.getState().getCode()
        );

        return PaycomResponse.success(id, result);
    }
}
