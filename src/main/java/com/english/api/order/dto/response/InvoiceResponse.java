package com.english.api.order.dto.response;

import com.english.api.order.model.enums.CurrencyType;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Response DTO for invoice data with formatted invoice information
 * Requirements: 5.5 - Standardized JSON responses
 */
public record InvoiceResponse(
        UUID id,
        UUID orderId,
        String number,
        Long totalCents,
        CurrencyType currency,
        String data, // JSON formatted invoice data
        OffsetDateTime createdAt
) {}