package com.english.api.order.dto.response;

import com.english.api.order.model.enums.OrderItemEntityType;

import java.util.UUID;

/**
 * Response DTO for order item data
 * Requirements: 5.5 - Standardized JSON responses
 */
public record OrderItemResponse(
        UUID id,
        OrderItemEntityType entityType,
        UUID entityId,
        String title,
        Integer quantity,
        Long unitPriceCents,
        Long totalPriceCents
) {}