package com.english.api.order.dto.response;

import com.english.api.order.model.enums.CurrencyType;
import com.english.api.order.model.enums.OrderStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Simplified order response for listing orders without detailed items information
 * Used for performance optimization in order listing endpoints
 */
public record OrderSummaryResponse(
        UUID id,
        UUID userId,
        OrderStatus status,
        CurrencyType currency,
        Long totalCents,
        OffsetDateTime createdAt,
        OffsetDateTime paidAt,
        int itemCount
) {}