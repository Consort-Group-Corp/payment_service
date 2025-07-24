package uz.consortgroup.payment_service.service.handler.click;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import uz.consortgroup.payment_service.dto.click.ClickRequest;
import uz.consortgroup.payment_service.dto.click.ClickResponse;
import uz.consortgroup.payment_service.exception.click.ClickException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClickServiceImpl implements ClickService {

    private final ApplicationContext applicationContext;
    private final Map<Integer, ClickMethodHandler> handlerMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void initHandlers() {
        Map<String, ClickMethodHandler> beans = applicationContext.getBeansOfType(ClickMethodHandler.class);
        for (ClickMethodHandler handler : beans.values()) {
            handlerMap.put(handler.getAction(), handler);
            log.info("Registered ClickMethodHandler: action={}, handlerClass={}",
                    handler.getAction(), handler.getClass().getSimpleName());
        }
    }

    @Override
    public ClickResponse handle(ClickRequest request) {
        Integer action = request.getAction();
        log.info("Handling Click request: action={}, clickTransactionId={}",
                action, request.getClickTransactionId());

        ClickMethodHandler handler = handlerMap.get(action);
        if (handler == null) {
            log.warn("No handler found for action={}", action);
            return ClickResponse.error(-8, "Method not found");
        }

        try {
            return handler.handle(request);
        } catch (ClickException e) {
            log.warn("ClickException occurred while handling action={}: code={}, message={}",
                    action, e.getCode(), e.getMessage());
            return ClickResponse.error(e);
        } catch (Exception e) {
            log.error("Unexpected error while handling action={}", action, e);
            return ClickResponse.error(-1000, "Internal server error");
        }
    }
}
