package com.english.api.order.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request DTO for creating a PayOS checkout session
 * Created by hungpham on 10/24/2025
 */
public record PayOSCheckoutRequest(
        @NotNull(message = "Order ID is required")
        UUID orderId
) {}