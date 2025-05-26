package uz.consortgroup.payment_service.dto.click;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ClickRequest {
    @NotNull(message = "Click transaction id is required")
    @JsonProperty("click_transaction_id")
    private Long clickTransactionId;

    @NotNull(message = "Service id is required")
    @JsonProperty("service_id")
    private Long serviceId;

    @NotNull(message = "Merchant transaction id is required")
    @JsonProperty("merchant_transaction_id")
    private String merchantTransactionId;

    @JsonProperty("merchant_prepare_id")
    private UUID merchantPrepareId;

    @NotNull(message = "Amount is required")
    @JsonProperty("amount")
    private Long amount;

    @NotNull(message = "Action is required")
    @JsonProperty("action")
    private Integer action;

    @NotNull(message = "Sign time is required")
    @JsonProperty("sign_time")
    private String signTime;

    @NotNull(message = "Sign string is required")
    @JsonProperty("sign_string")
    private String signString;

    @JsonProperty("error")
    private Integer error;

    @JsonProperty("error_note")
    private String errorNote;
}
