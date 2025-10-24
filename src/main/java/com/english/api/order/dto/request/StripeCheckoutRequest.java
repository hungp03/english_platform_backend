package com.english.api.order.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request DTO for creating a Stripe checkout session
 * Created by hungpham on 10/23/2025
 */
public record StripeCheckoutRequest(
        @NotNull(message = "Order ID is required")
        UUID orderId,

        @Email(message = "Valid customer email is required")
        String customerEmail
) {}
