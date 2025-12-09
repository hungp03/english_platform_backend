package com.english.api.order.controller;

import com.english.api.order.dto.request.PayOSCheckoutRequest;
import com.english.api.order.dto.request.PayPalCheckoutRequest;
import com.english.api.order.dto.response.PaymentResponse;
import com.english.api.order.dto.response.PayPalCheckoutResponse;
import com.english.api.order.service.PayOSPaymentService;
import com.english.api.order.service.PaymentService;
import com.english.api.order.service.PayPalPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.webhooks.Webhook;

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

    private final PayPalPaymentService payPalPaymentService;
    private final PayOSPaymentService payOSPaymentService;
    private final PaymentService paymentService;

    @PostMapping("/paypal/checkout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PayPalCheckoutResponse> createPayPalCheckout(
            @Valid @RequestBody PayPalCheckoutRequest request) {
        PayPalCheckoutResponse response = paymentService.createPayPalCheckout(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/paypal/capture/{paypalOrderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> capturePayPalOrder(@PathVariable String paypalOrderId) {
        payPalPaymentService.captureOrder(paypalOrderId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/payos/checkout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CreatePaymentLinkResponse> createPayOSCheckout(
            @Valid @RequestBody PayOSCheckoutRequest request) {
        CreatePaymentLinkResponse response = paymentService.createPayOSCheckout(request);
        return ResponseEntity.ok(response);
    }

    
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByOrder(
            @PathVariable UUID orderId) {
        List<PaymentResponse> payments = paymentService.getPaymentsByOrder(orderId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/{paymentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable UUID paymentId) {
        PaymentResponse payment = paymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(payment);
    }

    @PostMapping("/payos/webhook")
    public ResponseEntity<String> handlePayOSWebhook(@RequestBody Webhook webhook) {
        try {
            payOSPaymentService.handleWebhook(webhook);
            return ResponseEntity.ok("Webhook processed");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Webhook failed: " + e.getMessage());
        }
        // return ResponseEntity.ok("Webhook processed");
    }

    @PostMapping("/paypal/webhook")
    public ResponseEntity<String> handlePayPalWebhook(
            @RequestBody com.english.api.order.dto.request.PayPalWebhookRequest webhook) {
        try {
            payPalPaymentService.handleWebhook(webhook);
            return ResponseEntity.ok("Webhook processed");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Webhook failed: " + e.getMessage());
        }
    }
}

