package uz.consortgroup.payment_service.service.order;

import uz.consortgroup.payment_service.dto.order.OrderRequest;
import uz.consortgroup.payment_service.dto.order.OrderResponse;

public interface OrderService {
    OrderResponse create(OrderRequest request);
}
