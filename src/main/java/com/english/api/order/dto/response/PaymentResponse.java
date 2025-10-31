package com.english.api.order.dto.response;

import com.english.api.order.model.enums.PaymentProvider;
import com.english.api.order.model.enums.PaymentStatus;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Response DTO for payment data with provider transaction details
 */
public record PaymentResponse(
        UUID id,
        UUID orderId,
        PaymentProvider provider,
        String providerTxn,
        Long amountCents,
        PaymentStatus status,
        JsonNode rawPayload,
        OffsetDateTime createdAt,
        OffsetDateTime confirmedAt
) {}