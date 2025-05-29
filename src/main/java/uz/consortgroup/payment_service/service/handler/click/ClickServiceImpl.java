package uz.consortgroup.payment_service.service.handler.click;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import uz.consortgroup.payment_service.asspect.annotation.AllAspect;
import uz.consortgroup.payment_service.dto.click.ClickRequest;
import uz.consortgroup.payment_service.dto.click.ClickResponse;
import uz.consortgroup.payment_service.exception.click.ClickException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
        }
    }

    @Override
    @AllAspect
    public ClickResponse handle(ClickRequest request) {
        Integer action = request.getAction();
        ClickMethodHandler handler = handlerMap.get(action);

        if (handler == null) {
            return ClickResponse.error(-8, "Method not found");
        }

        try {
            return handler.handle(request);
        } catch (ClickException e) {
            return ClickResponse.error(e);
        } catch (Exception e) {
            return ClickResponse.error(-1000, "Internal server error");
        }
    }
}
