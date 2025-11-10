package com.english.api.cart.dto.response;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

public record CartResponse(
    List<CartItemResponse> items,
    CartSummary summary
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public record CartSummary(
        long totalPublishedCourses,
        Long totalPriceCents,
        String currency,
        int maxCartSize,
        boolean isCartFull
    ) implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
    }
}