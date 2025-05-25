package uz.consortgroup.payment_service.service.handler;

import uz.consortgroup.payment_service.dto.PaycomRequest;
import uz.consortgroup.payment_service.dto.PaycomResponse;

public interface PaycomMethodHandler {
    String getMethod();
    PaycomResponse handle(PaycomRequest request);
}
