package com.english.api.cart.dto.response;

import com.english.api.common.dto.PaginationResponse;

import java.io.Serial;
import java.io.Serializable;

public record CartPaginationResponse(
    PaginationResponse.Meta meta,
    Object result,
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
