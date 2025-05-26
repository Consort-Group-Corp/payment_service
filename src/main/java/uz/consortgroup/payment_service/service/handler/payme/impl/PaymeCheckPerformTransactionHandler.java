package uz.consortgroup.payment_service.service.handler.payme.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.consortgroup.payment_service.asspect.annotation.AllAspect;
import uz.consortgroup.payment_service.dto.paycom.PaycomRequest;
import uz.consortgroup.payment_service.dto.paycom.PaycomResponse;
import uz.consortgroup.payment_service.service.handler.payme.PaycomMethodHandler;

import java.util.Map;

@Service
public class PaymeCheckPerformTransactionHandler implements PaycomMethodHandler {

    @Override
    @Transactional(readOnly = true)
    @AllAspect
    public PaycomResponse handle(PaycomRequest request) {
        return PaycomResponse.success(request.getId(), Map.of("allow", true));
    }

    @Override
    public String getMethod() {
        return "CheckPerformTransaction";
    }
}
