package uz.consortgroup.payment_service.service;

import uz.consortgroup.payment_service.dto.PaycomRequestDto;
import uz.consortgroup.payment_service.dto.PaycomResponse;

public interface PaycomService {
    PaycomResponse handle(PaycomRequestDto request);
}
