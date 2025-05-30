package uz.consortgroup.payment_service.service.handler.payme;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import uz.consortgroup.payment_service.dto.paycom.PaycomRequest;
import uz.consortgroup.payment_service.dto.paycom.PaycomResponse;
import uz.consortgroup.payment_service.service.util.PaycomErrorFactory;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaycomServiceImplTest {

    @Mock
    private ApplicationContext ctx;

    @Mock
    private PaycomMethodHandler handlerA;

    private PaycomServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PaycomServiceImpl(ctx);
    }

    @Test
    void handle_knownMethod_returnsHandlerResponse() {
        PaycomRequest req = PaycomRequest.builder()
                .id(1L)
                .method("aMethod")
                .params(Map.of())
                .build();
        when(handlerA.getMethod()).thenReturn("aMethod");
        PaycomResponse ok = PaycomResponse.builder().id(1L).result("ok").build();
        when(handlerA.handle(req)).thenReturn(ok);
        when(ctx.getBeansOfType(PaycomMethodHandler.class))
                .thenReturn(Map.of("h1", handlerA));
        service.initHandlers();

        PaycomResponse resp = service.handle(req);

        assertThat(resp.getError()).isNull();
        assertThat(resp.getResult()).isEqualTo("ok");
    }

    @Test
    void handle_unknownMethod_returnsMethodNotFoundError() {
        PaycomRequest req = PaycomRequest.builder()
                .id(2L)
                .method("unknown")
                .params(Map.of())
                .build();
        when(ctx.getBeansOfType(PaycomMethodHandler.class))
                .thenReturn(Map.of());
        service.initHandlers();

        PaycomResponse resp = service.handle(req);

        assertThat(resp.getError()).isNotNull();
        assertThat(resp.getError().getCode())
                .isEqualTo(PaycomErrorFactory.methodNotFound().getCode());
    }

    @Test
    void handle_handlerThrowsException_returnsInternalError() {
        PaycomRequest req = PaycomRequest.builder()
                .id(3L)
                .method("aMethod")
                .params(Map.of())
                .build();
        when(handlerA.getMethod()).thenReturn("aMethod");
        when(ctx.getBeansOfType(PaycomMethodHandler.class))
                .thenReturn(Map.of("h1", handlerA));
        service.initHandlers();
        when(handlerA.handle(req)).thenThrow(new RuntimeException("boom"));

        PaycomResponse resp = service.handle(req);

        assertThat(resp.getError()).isNotNull();
        assertThat(resp.getError().getCode())
                .isEqualTo(PaycomErrorFactory.internalError().getCode());
    }
}
