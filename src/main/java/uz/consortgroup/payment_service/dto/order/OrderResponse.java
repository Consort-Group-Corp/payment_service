package uz.consortgroup.payment_service.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.consortgroup.payment_service.entity.OrderSource;
import uz.consortgroup.payment_service.entity.OrderStatus;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OrderResponse {
    private UUID id;
    private String externalOrderId;
    private Long amount;
    private OrderSource source;
    private OrderStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}