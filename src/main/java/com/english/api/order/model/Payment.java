package com.english.api.order.model;

import com.english.api.order.model.enums.CurrencyType;
import com.english.api.order.model.enums.PaymentProvider;
import com.english.api.order.model.enums.PaymentStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentProvider provider;

    @Column(name = "provider_txn")
    private String providerTxn;

    @Column(name = "amount_cents", nullable = false)
    private Long amountCents;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "currency", columnDefinition = "currency_type")
    private CurrencyType currency;

    @Column(name = "converted_amount_cents", precision = 20, scale = 2)
    private BigDecimal convertedAmountCents;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "converted_currency", columnDefinition = "currency_type")
    private CurrencyType convertedCurrency;

    @Column(name = "exchange_rate", precision = 20, scale = 10)
    private BigDecimal exchangeRate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.INITIATED;

    @Column(name = "raw_payload", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode rawPayload;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "confirmed_at")
    private OffsetDateTime confirmedAt;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UuidCreator.getTimeOrderedEpoch();
        }
    }
}
