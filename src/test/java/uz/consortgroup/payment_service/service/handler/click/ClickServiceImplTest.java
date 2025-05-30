package uz.consortgroup.payment_service.service.handler.click;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import uz.consortgroup.payment_service.dto.click.ClickRequest;
import uz.consortgroup.payment_service.dto.click.ClickResponse;
import uz.consortgroup.payment_service.exception.click.ClickException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ClickServiceImplTest {

    private ApplicationContext ctx;
    private ClickServiceImpl service;

    @BeforeEach
    void setUp() {
        ctx = mock(ApplicationContext.class);
        service = new ClickServiceImpl(ctx);
    }

    @Test
    void handle_validAction_returnsSuccess() {
        ClickMethodHandler handler = mock(ClickMethodHandler.class);
        when(handler.getAction()).thenReturn(1);
        ClickRequest req = new ClickRequest();
        req.setAction(1);
        ClickResponse resp = ClickResponse.success(123L, "mti", "mpi");
        when(handler.handle(req)).thenReturn(resp);

        when(ctx.getBeansOfType(ClickMethodHandler.class))
            .thenReturn(Map.of("h", handler));
        service.initHandlers();

        ClickResponse out = service.handle(req);
        assertEquals(0, out.getError());
        assertEquals(123L, out.getClick_trans_id());
    }

    @Test
    void handle_unknownAction_returnsMethodNotFoundError() {
        when(ctx.getBeansOfType(ClickMethodHandler.class))
            .thenReturn(Map.of());
        service.initHandlers();

        ClickRequest req = new ClickRequest();
        req.setAction(9);

        ClickResponse out = service.handle(req);
        assertEquals(-8, out.getError());
        assertEquals("Method not found", out.getError_note());
    }

    @Test
    void handle_handlerThrowsClickException_returnsErrorFromException() {
        ClickMethodHandler handler = mock(ClickMethodHandler.class);
        when(handler.getAction()).thenReturn(2);
        ClickRequest req = new ClickRequest();
        req.setAction(2);
        ClickException ex = new ClickException(-4, Map.of("en", "already paid"));
        when(handler.handle(req)).thenThrow(ex);

        when(ctx.getBeansOfType(ClickMethodHandler.class))
            .thenReturn(Map.of("h2", handler));
        service.initHandlers();

        ClickResponse out = service.handle(req);
        assertEquals(-4, out.getError());
        assertEquals("already paid", out.getError_note());
    }

    @Test
    void handle_handlerThrowsOtherException_returnsInternalServerError() {
        ClickMethodHandler handler = mock(ClickMethodHandler.class);
        when(handler.getAction()).thenReturn(3);
        ClickRequest req = new ClickRequest();
        req.setAction(3);
        when(handler.handle(req)).thenThrow(new RuntimeException("boom"));

        when(ctx.getBeansOfType(ClickMethodHandler.class))
            .thenReturn(Map.of("h3", handler));
        service.initHandlers();

        ClickResponse out = service.handle(req);
        assertEquals(-1000, out.getError());
        assertEquals("Internal server error", out.getError_note());
    }
}
