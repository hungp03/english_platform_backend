package com.english.api.order.dto.response;

import com.english.api.order.model.enums.VoucherDiscountType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record VoucherApplyResponse(
        String code,
        VoucherDiscountType discountType,
        BigDecimal discountValue,
        BigDecimal maxDiscountAmount,
        BigDecimal totalDiscount,
        List<CourseDiscountDetail> courseDiscounts,
        boolean valid,
        String message
) {
    public record CourseDiscountDetail(
            UUID courseId,
            String courseTitle,
            BigDecimal originalPrice,
            BigDecimal discountAmount,
            BigDecimal finalPrice
    ) {
    }
}
