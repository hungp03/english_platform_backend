package com.english.api.order.dto.response;

import com.english.api.order.model.enums.CurrencyType;
import com.english.api.order.model.enums.OrderStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Detailed order response with full information including user details, items, and payments
 * Used for single order retrieval endpoints
 */
public record OrderDetailResponse(
        UUID id,
        UserBasicInfo user,
        OrderStatus status,
        CurrencyType currency,
        Long totalCents,
        OffsetDateTime createdAt,
        OffsetDateTime paidAt,
        List<OrderItemResponse> items,
        List<PaymentSummaryResponse> payments
) {}