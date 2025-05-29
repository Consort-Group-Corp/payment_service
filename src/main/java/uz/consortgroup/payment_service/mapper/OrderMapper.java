package uz.consortgroup.payment_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import uz.consortgroup.payment_service.dto.order.OrderResponse;
import uz.consortgroup.payment_service.entity.Order;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "externalOrderId", source = "externalOrderId")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "source", source = "source")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    OrderResponse toDto(Order order);
}
