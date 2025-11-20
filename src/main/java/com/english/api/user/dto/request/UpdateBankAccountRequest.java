package com.english.api.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateBankAccountRequest(
        @NotBlank(message = "PayPal email is required")
        @Email(message = "Invalid email format")
        String paypalEmail
) {}
