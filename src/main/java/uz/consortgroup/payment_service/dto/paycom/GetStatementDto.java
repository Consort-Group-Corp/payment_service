package uz.consortgroup.payment_service.dto.paycom;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetStatementDto {
    private String transaction;
    private Long time;
    private Long amount;
    private Map<String, String> account;
    private Long create_time;
    private Long perform_time;
    private Long cancel_time;
    private Integer state;
    private Integer reason;
}
