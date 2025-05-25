package uz.consortgroup.payment_service.service.util;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static <T> T convertParams(Object raw, Class<T> clazz) {
        return objectMapper.convertValue(raw, clazz);
    }
}
