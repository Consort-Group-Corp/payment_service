package uz.consortgroup.payment_service.service.handler.click.impl;

import uz.consortgroup.payment_service.dto.click.ClickAction;
import uz.consortgroup.payment_service.dto.click.ClickRequest;
import uz.consortgroup.payment_service.dto.click.ClickResponse;
import uz.consortgroup.payment_service.service.handler.click.ClickMethodHandler;

public class ClickPaymentTransactionHandler implements ClickMethodHandler {
    @Override
    public Integer getAction() {
        return ClickAction.PAYMENT.getCode();
    }

    @Override
    public ClickResponse handle(ClickRequest request) {
        return null;
    }
}
