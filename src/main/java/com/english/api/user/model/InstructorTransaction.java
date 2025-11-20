package com.english.api.user.model;

import com.english.api.user.model.enums.TransactionType;
import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "instructor_transactions", indexes = {
    @Index(name = "idx_instructor_tx_user_id", columnList = "user_id"),
    @Index(name = "idx_instructor_tx_created_at", columnList = "created_at"),
    @Index(name = "idx_instructor_tx_reference_id", columnList = "reference_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstructorTransaction {
    
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(nullable = false, columnDefinition = "transaction_type")
    private TransactionType type;
    
    @Column(name = "amount_cents", nullable = false, precision = 19, scale = 2)
    private java.math.BigDecimal amountCents; // Positive for credit, negative for debit
    
    @Column(name = "balance_after_cents", nullable = false, precision = 19, scale = 2)
    private java.math.BigDecimal balanceAfterCents;
    
    @Column(name = "currency", length = 10, nullable = false)
    private String currency; // VND for sales, USD for withdrawals
    
    @Column(name = "reference_id")
    private UUID referenceId; // OrderItem ID, WithdrawalRequest ID, etc.
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
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
