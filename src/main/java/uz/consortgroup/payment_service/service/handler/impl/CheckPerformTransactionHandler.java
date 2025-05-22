package uz.consortgroup.payment_service.service.handler.impl;

import org.springframework.stereotype.Component;
import uz.consortgroup.payment_service.dto.PaycomRequestDto;
import uz.consortgroup.payment_service.dto.PaycomResponse;
import uz.consortgroup.payment_service.service.handler.PaycomMethodHandler;

import java.util.Map;

@Component("CheckPerformTransaction")
public class CheckPerformTransactionHandler implements PaycomMethodHandler {

    @Override
    public PaycomResponse handle(PaycomRequestDto request) {
        return PaycomResponse.success(request.getId(), Map.of("allow", true));
    }
}
