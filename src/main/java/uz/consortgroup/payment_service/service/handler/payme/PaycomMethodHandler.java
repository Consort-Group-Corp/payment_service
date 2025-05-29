package uz.consortgroup.payment_service.service.handler.payme;

import uz.consortgroup.payment_service.dto.paycom.PaycomRequest;
import uz.consortgroup.payment_service.dto.paycom.PaycomResponse;

public interface PaycomMethodHandler {
    String getMethod();
    PaycomResponse handle(PaycomRequest request);
}
