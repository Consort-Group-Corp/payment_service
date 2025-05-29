package uz.consortgroup.payment_service.dto.paycom;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.consortgroup.payment_service.exception.paycom.PaycomException;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaycomResponse {
    private Object id;
    private Object result;
    private PaycomError error;

    public static PaycomResponse success(Object id, Object result) {
        return PaycomResponse.builder()
                .id(id)
                .result(result)
                .build();
    }

    public static PaycomResponse error(Object id, PaycomError error) {
        return PaycomResponse.builder()
                .id(id)
                .error(error)
                .build();
    }

    public static PaycomResponse error(Object id, PaycomException ex) {
        return PaycomResponse.builder()
                .id(id)
                .error(PaycomError.builder()
                        .code(ex.getCode())
                        .message(ex.getMessages())
                        .data(ex.getData())
                        .build())
                .build();
    }
}
