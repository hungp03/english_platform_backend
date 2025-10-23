package com.english.api.order.dto.response;

import com.english.api.order.model.enums.RefundStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Response DTO for refund data
 * Requirements: 5.5 - Standardized JSON responses
 */
public record RefundResponse(
        UUID id,
        UUID paymentId,
        Long amountCents,
        String reason,
        RefundStatus status,
        OffsetDateTime createdAt
) {}