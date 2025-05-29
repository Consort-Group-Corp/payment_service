package uz.consortgroup.payment_service.exception.click;

import lombok.Getter;

import java.util.Map;

@Getter
public class ClickException extends RuntimeException {
    private final int code;
    private final Map<String, String> messages;

    public ClickException(int code, Map<String, String> messages) {
        super(messages.getOrDefault("en", "Error"));
        this.code = code;
        this.messages = messages;
    }
}
