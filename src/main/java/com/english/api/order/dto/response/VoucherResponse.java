package com.english.api.order.dto.response;

import com.english.api.order.model.enums.VoucherDiscountType;
import com.english.api.order.model.enums.VoucherScope;
import com.english.api.order.model.enums.VoucherStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record VoucherResponse(
        UUID id,
        String code,
        VoucherScope scope,
        VoucherDiscountType discountType,
        BigDecimal discountValue,
        BigDecimal maxDiscountAmount,
        BigDecimal minOrderAmount,
        Integer usageLimit,
        Integer usagePerUser,
        Integer usedCount,
        OffsetDateTime startDate,
        OffsetDateTime endDate,
        VoucherStatus status,
        List<VoucherCourseResponse> applicableCourses,
        OffsetDateTime createdAt
) {
    public record VoucherCourseResponse(
            UUID id,
            String title,
            String slug,
            String thumbnail
    ) {
    }
}
