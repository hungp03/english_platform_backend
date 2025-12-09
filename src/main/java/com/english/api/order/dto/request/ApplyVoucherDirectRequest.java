package com.english.api.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ApplyVoucherDirectRequest(
        @NotBlank(message = "Mã voucher không được để trống")
        String code,
        
        @NotNull(message = "ID khóa học không được để trống")
        UUID courseId
) {
}
