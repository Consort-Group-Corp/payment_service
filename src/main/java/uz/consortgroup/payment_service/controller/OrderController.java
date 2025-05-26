package uz.consortgroup.payment_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uz.consortgroup.payment_service.dto.order.OrderRequest;
import uz.consortgroup.payment_service.dto.order.OrderResponse;
import uz.consortgroup.payment_service.service.order.OrderService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/orders")
@Validated
public class OrderController {
    private final OrderService orderService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public OrderResponse create(@Valid @RequestBody OrderRequest request) {
        return  orderService.create(request);
    }
}
