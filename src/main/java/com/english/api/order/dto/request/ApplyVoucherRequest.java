package com.english.api.order.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ApplyVoucherRequest(
        @NotBlank(message = "Mã voucher không được để trống")
        String code
) {
}
