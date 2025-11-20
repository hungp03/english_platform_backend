package com.english.api.user.model;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "instructor_balances", indexes = {
    @Index(name = "idx_instructor_balance_user_id", columnList = "user_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstructorBalance {
    
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @Column(name = "available_balance_cents", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private java.math.BigDecimal availableBalanceCents = java.math.BigDecimal.ZERO;
    
    @Column(name = "pending_balance_cents", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private java.math.BigDecimal pendingBalanceCents = java.math.BigDecimal.ZERO;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UuidCreator.getTimeOrderedEpoch();
        }
    }
}
