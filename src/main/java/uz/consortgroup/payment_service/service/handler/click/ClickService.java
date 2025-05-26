package uz.consortgroup.payment_service.service.handler.click;

import uz.consortgroup.payment_service.dto.click.ClickRequest;
import uz.consortgroup.payment_service.dto.click.ClickResponse;

public interface ClickService {
    ClickResponse handle(ClickRequest request);
}
