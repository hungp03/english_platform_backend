package com.english.api.order.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request DTO for creating a PayPal checkout order
 */
public record PayPalCheckoutRequest(
        @NotNull(message = "Order ID is required")
        UUID orderId,

        @Email(message = "Valid customer email is required")
        String customerEmail
) {}
