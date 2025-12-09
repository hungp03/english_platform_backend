package com.english.api.order.service;

import com.english.api.order.dto.request.PayOSCheckoutRequest;
import com.english.api.order.dto.request.PayPalCheckoutRequest;
import com.english.api.order.dto.response.PaymentResponse;
import com.english.api.order.dto.response.PayPalCheckoutResponse;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for payment operations
 * Created by hungpham on 10/23/2025
 */
public interface PaymentService {

    /**
     * Create a checkout order for PayPal payment
     * @param request the checkout request
     * @return the checkout response with approval URL
     */
    PayPalCheckoutResponse createPayPalCheckout(PayPalCheckoutRequest request);

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

    CreatePaymentLinkResponse createPayOSCheckout(PayOSCheckoutRequest request);
}

