package com.english.api.order.service;

import com.english.api.order.dto.request.PayPalCheckoutRequest;
import com.english.api.order.dto.request.PayPalWebhookRequest;
import com.english.api.order.dto.response.PayPalCheckoutResponse;

/**
 * Service interface for PayPal payment operations
 */
public interface PayPalPaymentService {

    PayPalCheckoutResponse createCheckoutOrder(PayPalCheckoutRequest request);

    void captureOrder(String paypalOrderId);
    
    void handleWebhook(PayPalWebhookRequest webhookEvent);
}
