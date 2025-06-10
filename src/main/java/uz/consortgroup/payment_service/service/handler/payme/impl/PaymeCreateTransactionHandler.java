package uz.consortgroup.payment_service.service.handler.payme.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderSource;
import uz.consortgroup.payment_service.asspect.annotation.AllAspect;
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
    @AllAspect
    public PaycomResponse handle(PaycomRequest request) {
        Object id = request.getId();

        CreateTransactionParams params = convertParams(request.getParams(), CreateTransactionParams.class);
        Optional<PaymeTransaction> existing = paymeTransactionRepository.findByPaycomTransactionId(params.getId());

        if (existing.isPresent()) {
            return PaycomResponse.success(id, Map.of(
                    "create_time", existing.get().getCreateTime().toEpochMilli(),
                    "transaction", existing.get().getId().toString(),
                    "state", existing.get().getState().getCode()
            ));
        }

        String orderId = params.getAccount().getOrderId();
        Long amount = params.getAmount();

        Order order = orderValidatorService.validateOrderExists(orderId, OrderSource.PAYME);
        orderValidatorService.validateAmount(order, amount);
        orderValidatorService.validateOrderStatus(order);

        PaymeTransaction paymeTransaction = PaymeTransaction.builder()
                .paycomTransactionId(params.getId())
                .orderId(params.getAccount().getOrderId())
                .amount(params.getAmount())
                .state(PaymeTransactionState.CREATED)
                .createTime(Instant.now())
                .build();

        paymeTransactionRepository.save(paymeTransaction);

        Map<String, Object> result = Map.of(
                "create_time", paymeTransaction.getCreateTime().toEpochMilli(),
                "transaction", paymeTransaction.getId().toString(),
                "state", paymeTransaction.getState().getCode()
        );

        return PaycomResponse.success(id, result);
    }
}
