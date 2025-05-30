package uz.consortgroup.payment_service.dto.paycom;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PaycomRequest {
    @NotNull(message = "id is required")
    private Object id;

    @NotBlank(message = "method is required")
    private String method;

    @NotNull(message = "params is required")
    private Map<String, Object> params;
}
