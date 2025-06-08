package uz.consortgroup.payment_service.service.order;


import uz.consortgroup.core.api.v1.dto.payment.order.OrderRequest;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderResponse;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderSource;

public interface OrderService {
    OrderResponse create(OrderRequest request);
    void markAsPaidAndPublish(String externalOrderId, OrderSource source);
    void deleteByExternalOrderId(String externalOrderId);
}
