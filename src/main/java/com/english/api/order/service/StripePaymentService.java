package com.english.api.order.service;

import com.english.api.order.dto.request.StripeCheckoutRequest;
import com.english.api.order.dto.response.StripeCheckoutResponse;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;

/**
 * Service interface for Stripe payment operations
 * Created by hungpham on 10/23/2025
 */
public interface StripePaymentService {

    /**
     * Create a Stripe checkout session for an order
     * @param request the checkout request containing order ID and customer information
     * @return the checkout response with session ID and URL
     */
    StripeCheckoutResponse createCheckoutSession(StripeCheckoutRequest request);

    /**
     * Handle Stripe webhook events
     * @param payload the raw webhook payload
     * @param sigHeader the Stripe signature header
     * @return the processed event
     */
    Event handleWebhook(String payload, String sigHeader);

    /**
     * Process successful checkout session
     * @param session the Stripe checkout session
     */
    void processSuccessfulCheckout(Session session);
}
