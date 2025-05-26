package uz.consortgroup.payment_service.dto.click;

import lombok.Getter;

@Getter
public enum ClickAction {
    CHECK(0),
    PAYMENT(1),
    CANCEL(2);

    private final int code;

    ClickAction(int code) {
        this.code = code;
    }
}
