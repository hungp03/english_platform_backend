package com.english.api.order.dto.response;

/**
 * Response DTO for Stripe checkout session creation
 * Created by hungpham on 10/23/2025
 */
public record StripeCheckoutResponse(
        String sessionId,
        String checkoutUrl,
        String status
) {}
