package uz.consortgroup.payment_service.dto.paycom;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PerformTransactionParams {
    private String id;
    private Long time;
    private Long amount;
    private AccountDto accountDto;
}