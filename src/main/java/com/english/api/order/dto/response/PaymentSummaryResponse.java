package com.english.api.order.dto.response;

import com.english.api.order.model.enums.PaymentProvider;
import com.english.api.order.model.enums.PaymentStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PaymentSummaryResponse(
        UUID id,
        PaymentProvider provider,
        Long amountCents,
        PaymentStatus status,
        OffsetDateTime createdAt
) {}