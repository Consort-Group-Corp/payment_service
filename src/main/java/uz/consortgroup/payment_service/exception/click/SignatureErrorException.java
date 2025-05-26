package uz.consortgroup.payment_service.exception.click;

import java.util.Map;

public class SignatureErrorException extends ClickException {
    public SignatureErrorException() {
        super(-1, Map.of("ru", "Ошибка подписи", "en", "SIGN CHECK FAILED!"));
    }
}
