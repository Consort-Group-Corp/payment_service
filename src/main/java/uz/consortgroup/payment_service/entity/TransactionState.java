package uz.consortgroup.payment_service.entity;

import lombok.Getter;

@Getter
public enum TransactionState {
    CREATED(1),
    COMPLETED(2),
    CANCELED(-1);

    private final int code;

    TransactionState(int code) {
        this.code = code;
    }

}
