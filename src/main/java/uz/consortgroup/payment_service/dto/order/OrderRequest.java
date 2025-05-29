package uz.consortgroup.payment_service.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.consortgroup.payment_service.entity.OrderSource;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class OrderRequest {
    @NotBlank(message = "externalOrderId is required")
    @Size(min = 1, max = 50, message = "External order ID must be between 1 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "External order ID must contain only letters, numbers, underscores, or hyphens")
    private String externalOrderId;

    @NotNull(message = "amount is required")
    @Positive(message = "amount must be positive")
    private Long amount;

    @NotNull(message = "source is required")
    private OrderSource source;
}
