package com.english.api.order.service.impl;

import com.english.api.common.exception.ResourceInvalidException;
import com.english.api.common.exception.ResourceNotFoundException;
import com.english.api.order.model.Order;
import com.english.api.order.model.OrderItem;
import com.english.api.order.model.Payment;
import com.english.api.order.model.enums.OrderStatus;
import com.english.api.order.model.enums.PaymentProvider;
import com.english.api.order.model.enums.PaymentStatus;
import com.english.api.order.repository.OrderRepository;
import com.english.api.order.repository.PaymentRepository;
import com.english.api.order.service.PayOSPaymentService;
import com.english.api.mail.service.MailService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.PayOS;
import vn.payos.type.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayOSServiceImpl implements PayOSPaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper;
    private final PayOS payOS;
    private final MailService mailService;

    @Value("${payos.success-url}")
    private String defaultSuccessUrl;

    @Value("${payos.cancel-url}")
    private String defaultCancelUrl;

    // Tạo link thanh toán
    @Transactional
    @Override
    public CheckoutResponseData createPaymentLink(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new ResourceInvalidException("Order must be in PENDING status to create PayOS link");
        }

        paymentRepository.findTopByOrderIdAndProviderOrderByCreatedAtDesc(order.getId(), PaymentProvider.PAYOS)
                .ifPresent(payment -> {
                    if (payment.getStatus() == PaymentStatus.SUCCESS) {
                        throw new ResourceInvalidException("Order already has a successful PayOS payment");
                    }
                });

        String successUrl = String.format(
                "%s?orderId=%s",
                defaultSuccessUrl,
                order.getId()
        );

        String cancelUrl = String.format(
                "%s?orderId=%s",
                defaultCancelUrl,
                order.getId()
        );

        try {
            // Map OrderItem -> ItemData
            List<ItemData> items = order.getItems().stream()
                    .map(this::mapToItemData)
                    .toList();

            // Build payment data
            PaymentData paymentData = PaymentData.builder()
                    .orderCode(System.currentTimeMillis())
                    .amount(Math.toIntExact(order.getTotalCents()))
                    .description("Thanh toán đơn hàng")
                    .items(items)
                    .returnUrl(successUrl)
                    .cancelUrl(cancelUrl)
                    .build();

            CheckoutResponseData checkout = payOS.createPaymentLink(paymentData);

            // Lưu record Payment
            Payment payment = Payment.builder()
                    .order(order)
                    .provider(PaymentProvider.PAYOS)
                    .providerTxn(checkout.getPaymentLinkId())
                    .amountCents(order.getTotalCents())
                    .status(PaymentStatus.INITIATED)
                    .rawPayload(serializeToJson(checkout))
                    .build();
            paymentRepository.save(payment);

            log.info("Created PayOS link: {}", checkout.getCheckoutUrl());
            return checkout;

        } catch (Exception e) {
            throw new RuntimeException("Failed to create PayOS payment link: " + e.getMessage(), e);
        }
    }

    // Xử lý webhook callback
    @Transactional
    @Override
    public void handleWebhook(Webhook webhookBody) {
        try {
            WebhookData data = payOS.verifyPaymentWebhookData(webhookBody);
            log.info("PayOS webhook received for orderCode: {}", data.getOrderCode());

            // Lấy payment với eager loading để tránh N+1 queries
            Payment payment = paymentRepository.findByProviderTxnWithOrderDetails(data.getPaymentLinkId())
                    .orElseThrow(() -> new ResourceNotFoundException("Payment not found for PayOS link " + data.getPaymentLinkId()));

            Order order = payment.getOrder();

            if (payment.getStatus() != PaymentStatus.SUCCESS) {
                payment.setStatus(PaymentStatus.SUCCESS);
                payment.setConfirmedAt(OffsetDateTime.now());
                payment.setRawPayload(serializeToJson(webhookBody));
                paymentRepository.save(payment);

                if (order.getStatus() == OrderStatus.PENDING) {
                    order.setStatus(OrderStatus.PAID);
                    order.setPaidAt(OffsetDateTime.now(ZoneOffset.UTC));
                    orderRepository.save(order);
                    
                    // Gửi email thông báo thanh toán thành công
                    try {
                        mailService.sendPaymentSuccessEmail(
                            order.getUser().getEmail(),
                            order,
                            payment,
                            "payment-success-email"
                        );
                        log.info("Payment success email sent to: {}", order.getUser().getEmail());
                    } catch (Exception e) {
                        log.error("Failed to send payment success email to: {}", order.getUser().getEmail(), e);
                    }
                }
            }

        } catch (Exception e) {
            log.error("Error handling PayOS webhook", e);
            throw new RuntimeException("Failed to process PayOS webhook: " + e.getMessage(), e);
        }
    }

    
    // Helper methods
    private ItemData mapToItemData(OrderItem item) {
        return ItemData.builder()
                .name(item.getTitle())
                .quantity(item.getQuantity())
                .price(Math.toIntExact(item.getUnitPriceCents()))
                .build();
    }

    private JsonNode serializeToJson(Object obj) {
        try {
            return objectMapper.valueToTree(obj);
        } catch (Exception e) {
            return objectMapper.createObjectNode();
        }
    }
}
