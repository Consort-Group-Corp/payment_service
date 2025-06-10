package uz.consortgroup.payment_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderResponse;
import uz.consortgroup.payment_service.entity.Order;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "externalOrderId", source = "externalOrderId")
    @Mapping(target = "itemId", source = "itemId")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "source", source = "source")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    OrderResponse toDto(Order order);
}
