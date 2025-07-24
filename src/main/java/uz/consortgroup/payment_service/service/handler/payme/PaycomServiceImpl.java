package uz.consortgroup.payment_service.service.handler.payme;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import uz.consortgroup.payment_service.dto.paycom.PaycomRequest;
import uz.consortgroup.payment_service.dto.paycom.PaycomResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static uz.consortgroup.payment_service.service.util.PaycomErrorFactory.internalError;
import static uz.consortgroup.payment_service.service.util.PaycomErrorFactory.methodNotFound;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaycomServiceImpl implements PaycomService {

    private final ApplicationContext applicationContext;
    private final Map<String, PaycomMethodHandler> handlerMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void initHandlers() {
        Map<String, PaycomMethodHandler> beans = applicationContext.getBeansOfType(PaycomMethodHandler.class);
        for (PaycomMethodHandler handler : beans.values()) {
            handlerMap.put(handler.getMethod(), handler);
            log.info("Registered PaycomMethodHandler: method={}, handlerClass={}",
                    handler.getMethod(), handler.getClass().getSimpleName());
        }
    }

    @Override
    public PaycomResponse handle(PaycomRequest request) {
        String method = request.getMethod();
        Object id = request.getId();

        log.info("Handling Paycom request: method={}, requestId={}", method, id);

        PaycomMethodHandler handler = handlerMap.get(method);
        if (handler == null) {
            log.warn("Handler not found for method: {}", method);
            return PaycomResponse.error(id, methodNotFound());
        }

        try {
            return handler.handle(request);
        } catch (Exception e) {
            log.error("Unexpected error while handling method: {}, requestId={}", method, id, e);
            return PaycomResponse.error(id, internalError());
        }
    }
}
