package com.english.api.auth.dto.request;

import com.english.api.common.util.constant.ValidationPatterns;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Created by hungpham on 9/22/2025
 */
public record OTPResetRequest(
        @NotNull(message = "Email cannot be null")
        @Email(message = "Invalid email format")
        @Pattern(regexp = ValidationPatterns.EMAIL_PATTERN, message = "Email cannot contain special characters or spaces, only -, _, ., and + are allowed")
        String email,
        @NotNull(message = "OTP cannot be null")
        @Pattern(regexp = ValidationPatterns.OTP_PATTERN, message = "OTP not valid")
        String otp
) {
}