package uz.consortgroup.payment_service.exception.paycom;

import lombok.Getter;

import java.util.Map;

@Getter
public abstract class PaycomException extends RuntimeException {
    private final int code;
    private final Map<String, String> messages;
    private final String data;

    public PaycomException(int code, Map<String, String> messages, String data) {
        super(messages.get("ru"));
        this.code = code;
        this.messages = messages;
        this.data = data;
    }

    public PaycomException(int code, Map<String, String> message) {
        this(code, message, null);
    }
}
