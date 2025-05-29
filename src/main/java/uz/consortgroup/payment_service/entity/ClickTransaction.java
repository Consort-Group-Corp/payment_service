package uz.consortgroup.payment_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "click_transactions", schema = "payment_schema")
public class ClickTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "click_transaction_id", nullable = false)
    private Long clickTransactionId;

    @Column(name = "service_id", nullable = false)
    private Long serviceId;

    @Column(name = "merchant_transaction_id", nullable = false)
    private String merchantTransactionId;

    @Column(name = "merchant_prepare_id")
    private String merchantPrepareId;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "action", nullable = false)
    private Integer action;

    @Column(name = "sign_time", nullable = false)
    private Instant signTime;

    @Column(name = "sign_string", nullable = false)
    private String signString;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_state", nullable = false)
    private ClickTransactionState state;

    @Column(name = "perform_time")
    private Instant performTime;

    @Column(name = "cancel_time")
    private Instant cancelTime;

    @Column(name = "cancel_reason")
    private String cancelReason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }
}
