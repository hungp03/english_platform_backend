package com.english.api.order.service.impl;

import com.english.api.common.exception.ResourceNotFoundException;
import com.english.api.order.dto.request.StripeCheckoutRequest;
import com.english.api.order.dto.response.PaymentResponse;
import com.english.api.order.dto.response.StripeCheckoutResponse;
import com.english.api.order.mapper.OrderMapper;
import com.english.api.order.model.Payment;
import com.english.api.order.model.enums.PaymentProvider;
import com.english.api.order.repository.PaymentRepository;
import com.english.api.order.service.PaymentService;
import com.english.api.order.service.StripePaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Implementation of PaymentService
 * Created by hungpham on 10/23/2025
 */
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final StripePaymentService stripePaymentService;
    private final PaymentRepository paymentRepository;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public StripeCheckoutResponse createStripeCheckout(StripeCheckoutRequest request) {
        return stripePaymentService.createCheckoutSession(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByOrder(UUID orderId) {
        List<Payment> payments = paymentRepository.findByOrderIdOrderByCreatedAtDesc(orderId);
        return payments.stream()
                .map(orderMapper::toPaymentResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));
        return orderMapper.toPaymentResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByProviderTxn(PaymentProvider provider, String providerTxn) {
        Payment payment = paymentRepository.findByProviderAndProviderTxn(provider, providerTxn)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Payment not found for provider %s with transaction %s", provider, providerTxn)));
        return orderMapper.toPaymentResponse(payment);
    }
}
