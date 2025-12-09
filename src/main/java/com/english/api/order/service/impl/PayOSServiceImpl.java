package com.english.api.order.service.impl;

import com.english.api.common.exception.ResourceInvalidException;
import com.english.api.common.exception.ResourceNotFoundException;
import com.english.api.enrollment.service.EnrollmentService;
import com.english.api.order.model.Order;
import com.english.api.order.model.OrderItem;
import com.english.api.order.model.Payment;
import com.english.api.order.model.enums.OrderStatus;
import com.english.api.order.model.enums.PaymentProvider;
import com.english.api.order.model.enums.PaymentStatus;
import com.english.api.order.repository.OrderRepository;
import com.english.api.order.repository.PaymentRepository;
import com.english.api.order.service.PayOSPaymentService;
import com.english.api.order.service.InvoiceService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;
import vn.payos.model.webhooks.Webhook;

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
    private final InvoiceService invoiceService;
    private final EnrollmentService enrollmentService;
    private final com.english.api.user.service.InstructorWalletService instructorWalletService;

    @Value("${payos.success-url}")
    private String defaultSuccessUrl;

    @Value("${payos.cancel-url}")
    private String defaultCancelUrl;

    // Tạo link thanh toán
    @Transactional
    @Override
    public Object createPaymentLink(UUID orderId) {
        log.info("Creating PayOS payment link for orderId: {}", orderId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
        
        log.debug("Order found - ID: {}, Status: {}, TotalCents: {}", order.getId(), order.getStatus(), order.getTotalCents());

        if (order.getStatus() != OrderStatus.PENDING) {
            log.error("Order status is not PENDING. Current status: {}", order.getStatus());
            throw new ResourceInvalidException("Order must be in PENDING status to create PayOS link");
        }

        paymentRepository.findTopByOrderIdAndProviderOrderByCreatedAtDesc(order.getId(), PaymentProvider.PAYOS)
                .ifPresent(payment -> {
                    log.debug("Existing PayOS payment found - Status: {}", payment.getStatus());
                    if (payment.getStatus() == PaymentStatus.SUCCESS) {
                        log.error("Order already has a successful PayOS payment");
                        throw new ResourceInvalidException("Order already has a successful PayOS payment");
                    }
                });

        String successUrl = String.format("%s?orderId=%s", defaultSuccessUrl, order.getId());
        String cancelUrl = String.format("%s?orderId=%s", defaultCancelUrl, order.getId());
        
        log.debug("URLs configured - Success: {}, Cancel: {}", successUrl, cancelUrl);

        try {
            List<PaymentLinkItem> items = order.getItems().stream()
                    .map(this::mapToPaymentLinkItem)
                    .toList();
            
            log.debug("Mapped {} order items", items.size());

            CreatePaymentLinkRequest request = CreatePaymentLinkRequest.builder()
                    .orderCode(System.currentTimeMillis() / 1000)
                    .amount(order.getTotalCents())
                    .description("Thanh toan don hang")
                    .items(items)
                    .cancelUrl(cancelUrl)
                    .returnUrl(successUrl)
                    .build();
            
            log.debug("PaymentRequest built - OrderCode: {}, Amount: {}", request.getOrderCode(), request.getAmount());

            var paymentLink = payOS.paymentRequests().create(request);
            Payment payment = Payment.builder()
                    .order(order)
                    .provider(PaymentProvider.PAYOS)
                    .providerTxn(String.valueOf(paymentLink.getOrderCode()))
                    .amountCents(order.getTotalCents())
                    .status(PaymentStatus.INITIATED)
                    .rawPayload(serializeToJson(paymentLink))
                    .build();
            paymentRepository.save(payment);
            
            log.info("Payment record saved - ID: {}", payment.getId());

            return paymentLink;

        } catch (Exception e) {
            log.error("Failed to create PayOS payment link for orderId: {}. Error: {}", orderId, e.getMessage(), e);
            throw new RuntimeException("Failed to create PayOS payment link: " + e.getMessage(), e);
        }
    }

    // Xử lý webhook callback
    @Transactional
    @Override
    public void handleWebhook(Webhook webhookBody) {
        try {
            var data = payOS.webhooks().verify(webhookBody);
            log.info("Webhook verified - OrderCode: {}", data.getOrderCode());
            
            Payment payment = paymentRepository.findByProviderTxnWithOrderDetails(String.valueOf(data.getOrderCode()))
                    .orElseThrow(() -> new ResourceNotFoundException("Payment not found for OrderCode " + data.getOrderCode()));

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
                    
                    instructorWalletService.processOrderEarnings(order);
                    enrollmentService.createEnrollmentsAfterPayment(order);
                    invoiceService.generateAndSendInvoiceAsync(order, payment);
                }
            }

        } catch (Exception e) {
            log.error("Failed to process PayOS webhook: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to process PayOS webhook: " + e.getMessage(), e);
        }
    }

    
    // Helper methods
    private PaymentLinkItem mapToPaymentLinkItem(OrderItem item) {
        Long finalPrice = item.getUnitPriceCents() - (item.getDiscountCents() != null ? item.getDiscountCents() : 0L);
        
        return PaymentLinkItem.builder()
                .name(item.getTitle())
                .quantity(item.getQuantity())
                .price((long) Math.toIntExact(finalPrice))
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
