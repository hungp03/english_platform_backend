package com.english.api.order.service.impl;

import com.english.api.common.exception.ResourceInvalidException;
import com.english.api.common.exception.ResourceNotFoundException;
import com.english.api.order.dto.request.StripeCheckoutRequest;
import com.english.api.order.dto.response.StripeCheckoutResponse;
import com.english.api.order.model.Order;
import com.english.api.order.model.OrderItem;
import com.english.api.order.model.Payment;
import com.english.api.order.model.enums.OrderStatus;
import com.english.api.order.model.enums.PaymentProvider;
import com.english.api.order.model.enums.PaymentStatus;
import com.english.api.order.repository.OrderRepository;
import com.english.api.order.repository.PaymentRepository;
import com.english.api.order.service.StripePaymentService;
import com.english.api.mail.service.MailService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of Stripe payment service
 * Created by hungpham on 10/23/2025
 */
@Service
@RequiredArgsConstructor
public class StripePaymentServiceImpl implements StripePaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper;
    private final MailService mailService;

    @Value("${stripe.webhook-secret}")
    private String webhookSecret;

    @Value("${stripe.success-url}")
    private String defaultSuccessUrl;

    @Value("${stripe.cancel-url}")
    private String defaultCancelUrl;

    @Override
    @Transactional
    public StripeCheckoutResponse createCheckoutSession(StripeCheckoutRequest request) {
        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + request.orderId()));

        // Validate order status
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new ResourceInvalidException("Order must be in PENDING status to create checkout session");
        }

        // Check if payment already exists
        paymentRepository.findTopByOrderIdAndProviderOrderByCreatedAtDesc(order.getId(), PaymentProvider.STRIPE)
                .ifPresent(payment -> {
                    if (payment.getStatus() == PaymentStatus.SUCCESS) {
                        throw new ResourceInvalidException("Order already has a successful payment");
                    }
                });
        String successUrl = String.format(
                "%s?orderId=%s&session_id={CHECKOUT_SESSION_ID}",
                defaultSuccessUrl,
                order.getId()
        );
        String cancelUrl = String.format(
                "%s?orderId=%s&session_id={CHECKOUT_SESSION_ID}",
                defaultCancelUrl,
                order.getId()
        );
        try {
            // Build line items from order items
            List<SessionCreateParams.LineItem> lineItems = order.getItems().stream()
                    .map(this::buildLineItem)
                    .toList();

            // Build session params
            SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                    .addAllLineItem(lineItems)
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(cancelUrl)
                    .putMetadata("orderId", order.getId().toString())
                    .putMetadata("userId", order.getUser().getId().toString());
            // Add customer email if provided
            if (request.customerEmail() != null && !request.customerEmail().isEmpty()) {
                paramsBuilder.setCustomerEmail(request.customerEmail());
            }

            // Create Stripe session
            Session session = Session.create(paramsBuilder.build());

            // Create payment record
            Payment payment = Payment.builder()
                    .order(order)
                    .provider(PaymentProvider.STRIPE)
                    .providerTxn(session.getId())
                    .amountCents(order.getTotalCents())
                    .status(PaymentStatus.INITIATED)
                    .rawPayload(serializeToJson(session))
                    .build();
            paymentRepository.save(payment);
            return new StripeCheckoutResponse(session.getId(), session.getUrl(), "CREATED");

        } catch (StripeException e) {
            throw new RuntimeException("Failed to create Stripe checkout session: " + e.getMessage());
        }
    }

    @Override
    public Event handleWebhook(String payload, String sigHeader) {
        try {
            return Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            throw new RuntimeException("Invalid webhook signature");
        }
    }

    @Override
    @Transactional
    public void processSuccessfulCheckout(Session session) {
        try {
            // Extract order ID from metadata
            String orderIdStr = session.getMetadata().get("orderId");
            if (orderIdStr == null) {
                return;
            }

            UUID orderId = UUID.fromString(orderIdStr);

            // Fetch order
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

            // Find payment with eager loading để tránh N+1 queries
            Payment payment = paymentRepository.findByProviderAndProviderTxnWithOrderDetails(
                            PaymentProvider.STRIPE, session.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Payment not found for session: " + session.getId()));

            // Update payment status
            if (payment.getStatus() != PaymentStatus.SUCCESS) {
                payment.setStatus(PaymentStatus.SUCCESS);
                payment.setConfirmedAt(OffsetDateTime.now());
                payment.setRawPayload(serializeToJson(session));
                paymentRepository.save(payment);

                // Update order status
                if (order.getStatus() == OrderStatus.PENDING) {
                    order.setStatus(OrderStatus.PAID);
                    order.setPaidAt(OffsetDateTime.now(ZoneOffset.UTC));
                    orderRepository.save(order);
                    
                    // Gửi email thông báo thanh toán thành công
                    try {
                        mailService.sendPaymentSuccessEmail(
                            payment.getOrder().getUser().getEmail(),
                            payment.getOrder(),
                            payment,
                            "payment-success-email"
                        );
                    } catch (Exception ignored) {
                    }
                }

            } else {
//                "Payment already marked as successful"
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to process successful checkout", e);
        }
    }

    @Override
    public JsonNode getCheckoutSession(String sessionId) {
        try {
            Session session = Session.retrieve(sessionId);
            return objectMapper.readTree(session.toJson());
        } catch (StripeException e) {
            throw new RuntimeException("Failed to retrieve checkout session: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Stripe session to JSON", e);
        }
    }


    /**
     * Build Stripe line item from order item
     */
    private SessionCreateParams.LineItem buildLineItem(OrderItem orderItem) {
        return SessionCreateParams.LineItem.builder()
                .setQuantity(orderItem.getQuantity().longValue())
                .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency(orderItem.getOrder().getCurrency().name().toLowerCase())
                                .setUnitAmount(orderItem.getUnitPriceCents())
                                .setProductData(
                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                .setName(orderItem.getTitle())
                                                .build()
                                )
                                .build()
                )
                .build();
    }

    /**
     * Serialize object to JsonNode
     */
    private JsonNode serializeToJson(Object obj) {
        try {
            return objectMapper.valueToTree(obj);
        } catch (Exception e) {
            return objectMapper.createObjectNode();
        }
    }
}
