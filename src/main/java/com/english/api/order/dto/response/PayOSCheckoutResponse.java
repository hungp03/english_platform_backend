package com.english.api.order.dto.response;

/**
 * Response DTO for PayOS checkout session creation
 * Created by hungpham on 10/24/2025
 */
public record PayOSCheckoutResponse(
        String paymentLinkId,
        String checkoutUrl,
        String qrCode,
        Long orderCode,
        String status
) {}