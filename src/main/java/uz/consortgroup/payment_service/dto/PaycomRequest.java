package uz.consortgroup.payment_service.dto;

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
    private Object id;
    private String method;
    private Map<String, Object> params;
}
