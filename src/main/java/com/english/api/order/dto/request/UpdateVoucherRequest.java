package com.english.api.order.dto.request;

import com.english.api.order.model.enums.VoucherDiscountType;
import com.english.api.order.model.enums.VoucherScope;
import com.english.api.order.model.enums.VoucherStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

public record UpdateVoucherRequest(
        VoucherScope scope,

        VoucherDiscountType discountType,

        @Positive(message = "Giá trị giảm phải lớn hơn 0")
        BigDecimal discountValue,

        @PositiveOrZero(message = "Giới hạn giảm tối đa phải >= 0")
        BigDecimal maxDiscountAmount,

        @PositiveOrZero(message = "Giá trị đơn hàng tối thiểu phải >= 0")
        BigDecimal minOrderAmount,

        @Positive(message = "Giới hạn sử dụng phải > 0")
        Integer usageLimit,

        @Positive(message = "Số lần sử dụng/user phải > 0")
        Integer usagePerUser,

        OffsetDateTime startDate,

        OffsetDateTime endDate,

        VoucherStatus status,

        Set<UUID> courseIds
) {
}
