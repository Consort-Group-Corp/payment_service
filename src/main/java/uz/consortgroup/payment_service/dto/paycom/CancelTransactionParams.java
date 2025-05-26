package uz.consortgroup.payment_service.dto.paycom;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CancelTransactionParams {
    private String id;
    private Integer reason;
    @JsonProperty("account")
    private AccountDto accountDto;
}
