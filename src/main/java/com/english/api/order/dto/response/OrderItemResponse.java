package com.english.api.order.dto.response;

import com.english.api.order.model.enums.OrderItemEntityType;

import java.util.UUID;

public record OrderItemResponse(
        UUID id,
        OrderItemEntityType entityType,
        UUID entityId,
        String title,
        Integer quantity,
        Long unitPriceCents,
        Long totalPriceCents
) {}