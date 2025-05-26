package uz.consortgroup.payment_service.dto.click;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.consortgroup.payment_service.exception.click.ClickException;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClickResponse {
    private int error;
    private String error_note;
    private Long click_trans_id;
    private String merchant_trans_id;
    private String merchant_prepare_id;

    public static ClickResponse success(Long clickTransId, String merchantTransId, String merchantPrepareId) {
        return ClickResponse.builder()
                .error(0)
                .error_note("Success")
                .click_trans_id(clickTransId)
                .merchant_trans_id(merchantTransId)
                .merchant_prepare_id(merchantPrepareId)
                .build();
    }

    public static ClickResponse error(int errorCode, String errorNote) {
        return ClickResponse.builder()
                .error(errorCode)
                .error_note(errorNote)
                .build();
    }

    public static ClickResponse error(ClickException ex) {
        return ClickResponse.builder()
                .error(ex.getCode())
                .error_note(ex.getMessage())
                .build();
    }
}
