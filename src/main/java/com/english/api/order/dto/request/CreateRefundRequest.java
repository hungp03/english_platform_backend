package com.english.api.order.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a refund
 * Requirements: 4.1 - Refund processing with amount validation
 */
public record CreateRefundRequest(
        @NotNull(message = "Refund amount is required")
        @Positive(message = "Refund amount must be positive")
        Long amountCents,

        @Size(max = 500, message = "Refund reason cannot exceed 500 characters")
        String reason
) {}