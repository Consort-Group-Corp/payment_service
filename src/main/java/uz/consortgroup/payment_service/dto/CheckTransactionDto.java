package uz.consortgroup.payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CheckTransactionDto {
    private Long create_time;
    private Long perform_time;
    private Long cancel_time;
    private String transaction;
    private Integer state;
    private Integer reason;
}
