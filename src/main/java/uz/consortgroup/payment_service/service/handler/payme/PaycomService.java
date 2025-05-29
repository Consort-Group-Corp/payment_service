package uz.consortgroup.payment_service.service.handler.payme;

import uz.consortgroup.payment_service.dto.paycom.PaycomRequest;
import uz.consortgroup.payment_service.dto.paycom.PaycomResponse;

public interface PaycomService {
    PaycomResponse handle(PaycomRequest request);
}
