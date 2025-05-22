package uz.consortgroup.payment_service.service.handler;

import uz.consortgroup.payment_service.dto.PaycomRequestDto;
import uz.consortgroup.payment_service.dto.PaycomResponse;

public interface PaycomMethodHandler {
    PaycomResponse handle(PaycomRequestDto request);
}
