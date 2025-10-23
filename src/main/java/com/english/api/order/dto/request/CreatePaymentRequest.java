package com.english.api.order.dto.request;

import com.english.api.order.model.enums.PaymentProvider;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Request DTO for creating a payment
 * Requirements: 2.1 - Payment processing with provider-specific fields
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

        // Stripe specific fields
        String stripeSuccessUrl,
        String stripeCancelUrl,
        String stripeCustomerEmail
) {}