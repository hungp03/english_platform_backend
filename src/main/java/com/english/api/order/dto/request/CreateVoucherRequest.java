package com.english.api.order.dto.request;

import com.english.api.order.model.enums.VoucherDiscountType;
import com.english.api.order.model.enums.VoucherScope;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

public record CreateVoucherRequest(
        @NotBlank(message = "Mã voucher không được để trống")
        @Size(min = 3, max = 50, message = "Mã voucher phải từ 3-50 ký tự")
        @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Mã voucher chỉ chứa chữ in hoa, số, gạch ngang và gạch dưới")
        String code,

        @NotNull(message = "Phạm vi áp dụng không được để trống")
        VoucherScope scope,

        @NotNull(message = "Loại giảm giá không được để trống")
        VoucherDiscountType discountType,

        @NotNull(message = "Giá trị giảm không được để trống")
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

        @NotNull(message = "Ngày bắt đầu không được để trống")
        @FutureOrPresent(message = "Ngày bắt đầu phải từ hiện tại trở đi")
        OffsetDateTime startDate,

        @NotNull(message = "Ngày kết thúc không được để trống")
        @Future(message = "Ngày kết thúc phải trong tương lai")
        OffsetDateTime endDate,

        Set<UUID> courseIds
) {
}
