package com.english.api.order.dto.response;

/**
 * Response DTO for PayPal checkout order creation
 */
public record PayPalCheckoutResponse(
        String paypalOrderId,
        String approvalUrl,
        String status
) {}
