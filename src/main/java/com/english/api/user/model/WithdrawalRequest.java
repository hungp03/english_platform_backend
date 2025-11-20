package com.english.api.user.model;

import com.english.api.user.model.enums.WithdrawalStatus;
import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "withdrawal_requests", indexes = {
    @Index(name = "idx_withdrawal_user_id", columnList = "user_id"),
    @Index(name = "idx_withdrawal_status", columnList = "status"),
    @Index(name = "idx_withdrawal_created_at", columnList = "created_at")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WithdrawalRequest {
    
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "amount_cents", nullable = false, precision = 19, scale = 2)
    private java.math.BigDecimal amountCents;
    
    @Column(name = "original_amount_cents", nullable = false, precision = 19, scale = 2)
    private java.math.BigDecimal originalAmountCents;
    
    @Column(name = "original_currency", length = 10)
    private String originalCurrency;
    
    @Column(name = "exchange_rate", precision = 19, scale = 10)
    private java.math.BigDecimal exchangeRate;
    
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(nullable = false, columnDefinition = "withdrawal_status")
    @Builder.Default
    private WithdrawalStatus status = WithdrawalStatus.PENDING;
    
    @Column(name = "bank_info_json", columnDefinition = "TEXT")
    private String bankInfoJson; // Snapshot of bank details at time of request
    
    @Column(name = "admin_note", columnDefinition = "TEXT")
    private String adminNote;
    
    @Column(name = "paypal_payout_batch_id")
    private String paypalPayoutBatchId;
    
    @Column(name = "paypal_payout_item_id")
    private String paypalPayoutItemId;
    
    @Column(name = "processed_at")
    private Instant processedAt;
    
    @Column(name = "completed_at")
    private Instant completedAt;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UuidCreator.getTimeOrderedEpoch();
        }
    }
}
