package com.english.api.order.controller;

import com.english.api.order.dto.request.PayOSCheckoutRequest;
import com.english.api.order.dto.request.StripeCheckoutRequest;
import com.english.api.order.dto.response.PaymentResponse;
import com.english.api.order.dto.response.StripeCheckoutResponse;
import com.english.api.order.service.PayOSPaymentService;
import com.english.api.order.service.PaymentService;
import com.english.api.order.service.StripePaymentService;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.Webhook;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for payment operations
 * Created by hungpham on 10/23/2025
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final StripePaymentService stripePaymentService;
    private final PayOSPaymentService payOSPaymentService;
    private final PaymentService paymentService;

    @PostMapping("/stripe/checkout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StripeCheckoutResponse> createStripeCheckout(
            @Valid @RequestBody StripeCheckoutRequest request) {
        StripeCheckoutResponse response = paymentService.createStripeCheckout(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/payos/checkout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CheckoutResponseData> createPayOSCheckout(
            @Valid @RequestBody PayOSCheckoutRequest request) {
        CheckoutResponseData response = paymentService.createPayOSCheckout(request);
        return ResponseEntity.ok(response);
    }

    
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByOrder(
            @PathVariable UUID orderId) {
        List<PaymentResponse> payments = paymentService.getPaymentsByOrder(orderId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable UUID paymentId) {
        PaymentResponse payment = paymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(payment);
    }

    @PostMapping("/stripe/webhook")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        try {
            Event event = stripePaymentService.handleWebhook(payload, sigHeader);
            switch (event.getType()) {
                case "checkout.session.completed":
                    Session session = (Session) event.getDataObjectDeserializer()
                            .getObject()
                            .orElseThrow(() -> new RuntimeException("Failed to deserialize session"));
                    stripePaymentService.processSuccessfulCheckout(session);
                    break;
                case "checkout.session.expired":
                case "payment_intent.succeeded":
                case "payment_intent.payment_failed":
                    break;
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Webhook processing failed");
        }
    }

    @PostMapping("/payos/webhook")
    public ResponseEntity<String> handlePayOSWebhook(@RequestBody Webhook webhook) {
        try {
            payOSPaymentService.handleWebhook(webhook);
            return ResponseEntity.ok("Webhook processed");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Webhook failed: " + e.getMessage());
        }
    }
}

