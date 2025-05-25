package uz.consortgroup.payment_service.service;

import uz.consortgroup.payment_service.dto.PaycomRequest;
import uz.consortgroup.payment_service.dto.PaycomResponse;

public interface PaycomService {
    PaycomResponse handle(PaycomRequest request);
}
