package com.english.api.order.service;

import com.english.api.order.dto.request.PayOSCheckoutRequest;
import com.english.api.order.dto.request.StripeCheckoutRequest;
import com.english.api.order.dto.response.PayOSCheckoutResponse;
import com.english.api.order.dto.response.PaymentResponse;
import com.english.api.order.dto.response.StripeCheckoutResponse;
import com.english.api.order.model.enums.PaymentProvider;
import jakarta.validation.Valid;
import vn.payos.type.CheckoutResponseData;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for payment operations
 * Created by hungpham on 10/23/2025
 */
public interface PaymentService {

    /**
     * Create a checkout session for Stripe payment
     * @param request the checkout request
     * @return the checkout response with session URL
     */
    StripeCheckoutResponse createStripeCheckout(StripeCheckoutRequest request);

    /**
     * Get payments by order ID
     * @param orderId the order ID
     * @return list of payment responses
     */
    List<PaymentResponse> getPaymentsByOrder(UUID orderId);

    /**
     * Get payment by ID
     * @param paymentId the payment ID
     * @return the payment response
     */
    PaymentResponse getPaymentById(UUID paymentId);

    /**
     * Get payment by provider transaction ID
     * @param provider the payment provider
     * @param providerTxn the provider transaction ID
     * @return the payment response
     */
    PaymentResponse getPaymentByProviderTxn(PaymentProvider provider, String providerTxn);

    CheckoutResponseData createPayOSCheckout(PayOSCheckoutRequest request);
}

