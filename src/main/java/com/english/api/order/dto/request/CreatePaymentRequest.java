package com.english.api.order.dto.request;

import com.english.api.order.model.enums.PaymentProvider;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Request DTO for creating a payment
 */
public record CreatePaymentRequest(
        @NotNull(message = "Payment provider is required")
        PaymentProvider provider,

        @Pattern(regexp = "^https?://.*", message = "Return URL must be a valid HTTP/HTTPS URL")
        String returnUrl,

        @Pattern(regexp = "^https?://.*", message = "Cancel URL must be a valid HTTP/HTTPS URL")
        String cancelUrl,

        // MoMo specific fields
        String momoOrderInfo,
        String momoRequestId,
        String momoExtraData,

        // VNPay specific fields
        String vnpayOrderInfo,
        String vnpayOrderType,
        String vnpayLocale,

        // PayPal specific fields
        String paypalSuccessUrl,
        String paypalCancelUrl,
        String paypalPayerEmail
) {}