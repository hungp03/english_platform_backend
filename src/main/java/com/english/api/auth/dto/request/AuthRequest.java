package com.english.api.auth.dto.request;

import com.english.api.common.util.constant.ValidationPatterns;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Created by hungpham on 9/22/2025
 */
public record AuthRequest(

        @NotBlank(message = "Identifier is required")
        @Pattern(
                regexp = ValidationPatterns.IDENTIFIER_PATTERN,
                message = "Identifier must not contain spaces or special characters except '.', '_', '+', '-', and '@'"
        )
        String identifier,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        @Pattern(
                regexp = ValidationPatterns.PASSWORD_PATTERN,
                message = "Password must not contain spaces"
        )
        String password
) {
}
