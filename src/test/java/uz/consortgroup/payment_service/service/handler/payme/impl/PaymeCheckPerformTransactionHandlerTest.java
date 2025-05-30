package uz.consortgroup.payment_service.service.handler.payme.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uz.consortgroup.payment_service.dto.paycom.PaycomRequest;
import uz.consortgroup.payment_service.dto.paycom.PaycomResponse;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class PaymeCheckPerformTransactionHandlerTest {

    private final PaymeCheckPerformTransactionHandler handler = new PaymeCheckPerformTransactionHandler();

    @Test
    void getMethod_returnsCorrectName() {
        assertThat(handler.getMethod()).isEqualTo("CheckPerformTransaction");
    }

    @Test
    void handle_withValidRequest_returnsAllowTrue() {
        PaycomRequest req = new PaycomRequest();
        req.setId("abc");
        PaycomResponse resp = handler.handle(req);

        assertThat(resp.getId()).isEqualTo("abc");
        assertThat(resp.getError()).isNull();
        assertThat(resp.getResult()).isEqualTo(Map.of("allow", true));
    }

    @Test
    void handle_nullRequest_throwsNullPointer() {
        assertThatThrownBy(() -> handler.handle(null))
            .isInstanceOf(NullPointerException.class);
    }
}
