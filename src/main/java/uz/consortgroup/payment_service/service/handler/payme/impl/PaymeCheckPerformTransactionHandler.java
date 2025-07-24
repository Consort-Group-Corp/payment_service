package uz.consortgroup.payment_service.service.handler.payme.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.payment_service.dto.paycom.PaycomRequest;
import uz.consortgroup.payment_service.dto.paycom.PaycomResponse;
import uz.consortgroup.payment_service.service.handler.payme.PaycomMethodHandler;

import java.util.Map;

@Service
@Slf4j
public class PaymeCheckPerformTransactionHandler implements PaycomMethodHandler {

    @Override
    @Transactional(readOnly = true)
    public PaycomResponse handle(PaycomRequest request) {
        log.info("Handling Payme CheckPerformTransaction request: requestId={}", request.getId());
        return PaycomResponse.success(request.getId(), Map.of("allow", true));
    }

    @Override
    public String getMethod() {
        return "CheckPerformTransaction";
    }
}
