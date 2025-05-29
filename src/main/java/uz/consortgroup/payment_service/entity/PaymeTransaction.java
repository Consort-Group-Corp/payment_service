package uz.consortgroup.payment_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "payme_transactions", schema = "payment_schema")
public class PaymeTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "paycom_transaction_id", nullable = false, unique = true, length = 50)
    private String paycomTransactionId;

    @Column(name = "order_id", nullable = false, length = 50)
    private String orderId;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "transaction_state", nullable = false, length = 100)
    @Enumerated(EnumType.STRING)
    private PaymeTransactionState state;

    @Column(name = "create_time", nullable = false)
    private Instant createTime;

    @Column(name = "perform_time")
    private Instant performTime;

    @Column(name = "cancel_time")
    private Instant cancelTime;

    @Column(name = "reason")
    private Integer reason;
}
