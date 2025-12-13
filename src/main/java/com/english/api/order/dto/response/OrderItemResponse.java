package com.english.api.order.dto.response;

import java.util.UUID;

public record OrderItemResponse(
        UUID id,
        UUID courseId,
        String title,
        Integer quantity,
        Long unitPriceCents,
        Long discountCents,
        Long totalPriceCents
) {}