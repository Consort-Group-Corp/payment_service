package uz.consortgroup.payment_service.service.handler.payme;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import uz.consortgroup.payment_service.asspect.annotation.AllAspect;
import uz.consortgroup.payment_service.dto.paycom.PaycomRequest;
import uz.consortgroup.payment_service.dto.paycom.PaycomResponse;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static uz.consortgroup.payment_service.service.util.PaycomErrorFactory.internalError;
import static uz.consortgroup.payment_service.service.util.PaycomErrorFactory.methodNotFound;

@RequiredArgsConstructor
@Service
public class PaycomServiceImpl implements PaycomService {

    private final ApplicationContext applicationContext;
    private final Map<String, PaycomMethodHandler> handlerMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void initHandlers() {
        Map<String, PaycomMethodHandler> beans = applicationContext.getBeansOfType(PaycomMethodHandler.class);
        for (PaycomMethodHandler handler : beans.values()) {
            handlerMap.put(handler.getMethod(), handler);
        }
    }


    @Override
    @AllAspect
    public PaycomResponse handle(PaycomRequest request) {
        String method = request.getMethod();
        PaycomMethodHandler handler = handlerMap.get(method);

        if (handler == null) {
            return PaycomResponse.error(request.getId(), methodNotFound());
        }

        try {
            return handler.handle(request);
        } catch (Exception e) {
            return PaycomResponse.error(request.getId(), internalError());
        }

    }
}
