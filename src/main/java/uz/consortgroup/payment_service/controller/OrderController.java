package uz.consortgroup.payment_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderRequest;
import uz.consortgroup.core.api.v1.dto.payment.order.OrderResponse;
import uz.consortgroup.payment_service.service.order.OrderService;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/orders")
@Validated
@Tag(name = "Order Management", description = "API для создания и управления платежными заказами")
public class OrderController {
    private final OrderService orderService;

    @Operation(
            summary = "Создание нового платежного заказа",
            description = "Создает новый заказ для проведения платежа через выбранный платежный провайдер."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Заказ успешно создан",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Невалидные данные запроса",
                    content = @Content(examples = @ExampleObject(value = """
                {
                    "timestamp": "2025-08-21T10:00:00Z",
                    "status": 400,
                    "error": "Bad Request",
                    "message": "userId is required"
                }
            """))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Заказ с таким externalOrderId уже существует",
                    content = @Content(examples = @ExampleObject(value = """
                {
                    "timestamp": "2025-08-21T10:00:00Z",
                    "status": 409,
                    "error": "Conflict",
                    "message": "Order with external ID already exists"
                }
            """))
            )
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public OrderResponse create(@Valid @RequestBody OrderRequest request) {
        return orderService.create(request);
    }

    @Operation(
            summary = "Удаление заказа по внешнему идентификатору",
            description = "Удаляет заказ используя внешний идентификатор (externalOrderId). " +
                    "Операция выполняется только если заказ находится в статусе NEW."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Заказ успешно удален"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Заказ с указанным externalOrderId не найден",
                    content = @Content(examples = @ExampleObject(value = """
                {
                    "timestamp": "2025-08-21T10:00:00Z",
                    "status": 404,
                    "error": "Not Found",
                    "message": "Order not found with externalOrderId: ORD-2025-000123"
                }
            """))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Невозможно удалить заказ в текущем статусе",
                    content = @Content(examples = @ExampleObject(value = """
                {
                    "timestamp": "2025-08-21T10:00:00Z",
                    "status": 409,
                    "error": "Conflict",
                    "message": "Cannot delete order in PROCESSING status"
                }
            """))
            )
    })
    @DeleteMapping("/{externalOrderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrder(
            @Parameter(
                    description = "Внешний идентификатор заказа",
                    example = "ORD-2025-000123",
                    required = true
            )
            @PathVariable String externalOrderId
    ) {
        orderService.deleteByExternalOrderId(externalOrderId);
    }
}