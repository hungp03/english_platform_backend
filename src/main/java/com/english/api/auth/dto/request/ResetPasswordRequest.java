package com.english.api.auth.dto.request;

import com.english.api.common.util.constant.ValidationPatterns;
import jakarta.validation.constraints.*;

/**
 * Created by hungpham on 9/22/2025
 */
public record ResetPasswordRequest(
        @NotNull(message = "Email cannot be null")
        @Email(message = "Invalid email format")
        @Pattern(regexp = ValidationPatterns.EMAIL_PATTERN, message = "Email cannot contain special characters or spaces, only -, _, ., and + are allowed")
        String email,
        @NotBlank(message = "Identify code is missing")
        String identifyCode,
        @NotNull(message = "Password cannot be null")
        @Size(min = 8, message = "Password must be at least 8 characters")
        @Pattern(regexp = ValidationPatterns.PASSWORD_PATTERN, message = "Password cannot contain spaces")
        String newPassword,

        @NotNull(message = "Confirm Password cannot be null")
        String confirmPassword) {
}