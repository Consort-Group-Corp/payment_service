package uz.consortgroup.payment_service.entity;

public enum OrderStatus {
    NEW,
    PAID,
    CANCELLED,
    FAILED;

    public boolean isPayable() {
        return this == NEW;
    }
}