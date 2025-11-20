package com.english.api.order.service.impl;

import com.english.api.common.exception.ResourceInvalidException;
import com.english.api.common.exception.ResourceNotFoundException;
import com.english.api.enrollment.service.EnrollmentService;
import com.english.api.order.config.PayPalProperties;
import com.english.api.order.dto.request.PayPalCheckoutRequest;
import com.english.api.order.dto.request.PayPalWebhookRequest;
import com.english.api.order.dto.response.PayPalCheckoutResponse;
import com.english.api.order.model.Order;
import com.english.api.order.model.OrderItem;
import com.english.api.order.model.Payment;
import com.english.api.order.model.enums.CurrencyType;
import com.english.api.order.model.enums.OrderStatus;
import com.english.api.order.model.enums.PaymentProvider;
import com.english.api.order.model.enums.PaymentStatus;
import com.english.api.order.repository.OrderRepository;
import com.english.api.order.repository.PaymentRepository;
import com.english.api.order.service.ExchangeRateService;
import com.english.api.order.service.InvoiceService;
import com.english.api.order.service.PayPalPaymentService;
import com.english.api.order.service.paypal.PayPalClient;
import com.english.api.user.service.InstructorWalletService;
import com.english.api.user.service.WithdrawalService;
import com.english.api.notification.service.NotificationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PayPalPaymentServiceImpl implements PayPalPaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper;
    private final PayPalClient payPalClient;
    private final PayPalProperties payPalProperties;
    private final InvoiceService invoiceService;
    private final EnrollmentService enrollmentService;
    private final ExchangeRateService exchangeRateService;
    private final InstructorWalletService instructorWalletService;
    private final WithdrawalService withdrawalService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public PayPalCheckoutResponse createCheckoutOrder(PayPalCheckoutRequest request) {
        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + request.orderId()));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new ResourceInvalidException("Order must be in PENDING status to create checkout order");
        }

        paymentRepository.findTopByOrderIdAndProviderOrderByCreatedAtDesc(order.getId(), PaymentProvider.PAYPAL)
                .ifPresent(payment -> {
                    if (payment.getStatus() == PaymentStatus.SUCCESS) {
                        throw new ResourceInvalidException("Order already has a successful payment");
                    }
                });

        CurrencyType originalCurrency = order.getCurrency();
        Long originalAmount = order.getTotalCents();
        CurrencyType paymentCurrency = originalCurrency;
        BigDecimal paymentAmount = BigDecimal.valueOf(originalAmount);
        BigDecimal exchangeRate = null;

        if (originalCurrency == CurrencyType.VND) {
            exchangeRate = exchangeRateService.getExchangeRate(CurrencyType.VND, CurrencyType.USD);
            paymentAmount = exchangeRateService.convertAmount(originalAmount, CurrencyType.VND, CurrencyType.USD);
            paymentCurrency = CurrencyType.USD;
        }

        ObjectNode payload = buildOrderPayload(order, paymentCurrency, paymentAmount, request.customerEmail());
        JsonNode response = payPalClient.createOrder(payload);

        String paypalOrderId = response.path("id").asText(null);
        if (paypalOrderId == null) {
            throw new ResourceInvalidException("PayPal order response missing ID");
        }

        String approvalUrl = extractLink(response, "approve")
                .orElseThrow(() -> new ResourceInvalidException("PayPal order response missing approval link"));

        Payment payment = Payment.builder()
                .order(order)
                .provider(PaymentProvider.PAYPAL)
                .providerTxn(paypalOrderId)
                .amountCents(originalAmount)
                .currency(originalCurrency)
                .convertedAmountCents(paymentAmount)
                .convertedCurrency(paymentCurrency)
                .exchangeRate(exchangeRate)
                .status(PaymentStatus.INITIATED)
                .rawPayload(response)
                .build();
        paymentRepository.save(payment);

        return new PayPalCheckoutResponse(paypalOrderId, approvalUrl, response.path("status").asText("CREATED"));
    }

    @Override
    @Transactional
    public void captureOrder(String paypalOrderId) {
        Payment payment = paymentRepository.findByProviderAndProviderTxnWithOrderDetails(PaymentProvider.PAYPAL, paypalOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for PayPal order: " + paypalOrderId));

        JsonNode response = payPalClient.captureOrder(paypalOrderId);
        String status = response.path("status").asText("");

        if ("COMPLETED".equalsIgnoreCase(status)) {
            markPaymentSuccessful(payment, response);
        } else if ("PAYER_ACTION_REQUIRED".equalsIgnoreCase(status) || "APPROVED".equalsIgnoreCase(status)) {
            payment.setStatus(PaymentStatus.PROCESSING);
            payment.setRawPayload(response);
            paymentRepository.save(payment);
            throw new ResourceInvalidException("PayPal payment requires additional payer action");
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setRawPayload(response);
            paymentRepository.save(payment);
            throw new ResourceInvalidException("PayPal capture failed with status: " + status);
        }
    }

    @Override
    @Transactional
    public void handleWebhook(PayPalWebhookRequest webhookEvent) {
        String eventType = webhookEvent.eventType();
        
        if ("PAYMENT.CAPTURE.COMPLETED".equalsIgnoreCase(eventType)) {
            handlePaymentCaptureCompleted(webhookEvent);
        } else if ("CHECKOUT.ORDER.APPROVED".equalsIgnoreCase(eventType)) {
            handleCheckoutOrderApproved(webhookEvent);
        } else if ("PAYMENT.PAYOUTSBATCH.DENIED".equalsIgnoreCase(eventType)) {
            handlePayoutBatchDenied(webhookEvent);
        } else if ("PAYMENT.PAYOUTSBATCH.SUCCESS".equalsIgnoreCase(eventType)) {
            handlePayoutBatchSuccess(webhookEvent);
        } else {
            // Log other events but don't process
            return;
        }
    }

    private void handlePaymentCaptureCompleted(PayPalWebhookRequest webhookEvent) {
        try {
            Map<String, Object> resource = webhookEvent.resource();
            
            // Get the order ID from supplementary_data
            Object supplementaryData = resource.get("supplementary_data");
            if (supplementaryData instanceof Map) {
                Map<String, Object> suppData = (Map<String, Object>) supplementaryData;
                Object relatedIds = suppData.get("related_ids");
                if (relatedIds instanceof Map) {
                    Map<String, Object> ids = (Map<String, Object>) relatedIds;
                    String orderId = (String) ids.get("order_id");
                    
                    if (orderId != null) {
                        Payment payment = paymentRepository.findByProviderAndProviderTxnWithOrderDetails(
                                PaymentProvider.PAYPAL, orderId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                        "Payment not found for PayPal order: " + orderId));
                        
                        JsonNode webhookJson = objectMapper.valueToTree(webhookEvent);
                        markPaymentSuccessful(payment, webhookJson);
                    }
                }
            }
        } catch (Exception e) {
            throw new ResourceInvalidException("Failed to process PayPal webhook: " + e.getMessage());
        }
    }

    private void handleCheckoutOrderApproved(PayPalWebhookRequest webhookEvent) {
        try {
            Map<String, Object> resource = webhookEvent.resource();
            String paypalOrderId = (String) resource.get("id");
            
            if (paypalOrderId != null) {
                Payment payment = paymentRepository.findByProviderAndProviderTxnWithOrderDetails(
                        PaymentProvider.PAYPAL, paypalOrderId)
                        .orElse(null);
                
                if (payment != null && payment.getStatus() == PaymentStatus.INITIATED) {
                    payment.setStatus(PaymentStatus.PROCESSING);
                    JsonNode webhookJson = objectMapper.valueToTree(webhookEvent);
                    payment.setRawPayload(webhookJson);
                    paymentRepository.save(payment);
                    
                    // Automatically capture the approved payment
                    captureOrder(paypalOrderId);
                }
            }
        } catch (Exception e) {
            // Log but don't fail - webhook will retry if capture fails
        }
    }

    private void markPaymentSuccessful(Payment payment, JsonNode payload) {
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return;
        }

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setConfirmedAt(OffsetDateTime.now(ZoneOffset.UTC));
        payment.setRawPayload(payload);
        paymentRepository.save(payment);

        Order order = payment.getOrder();
        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.PAID);
            order.setPaidAt(OffsetDateTime.now(ZoneOffset.UTC));
            orderRepository.save(order);
            
            // Credit instructors for their course sales
            instructorWalletService.processOrderEarnings(order);
            
            enrollmentService.createEnrollmentsAfterPayment(order);
            invoiceService.generateAndSendInvoiceAsync(order, payment);
            
            // Notify user about successful payment
            notificationService.sendNotification(
                order.getUser().getId(),
                "Thanh toán thành công",
                "Đơn hàng #" + order.getId() + " đã được thanh toán thành công. Cảm ơn bạn đã sử dụng dịch vụ của chúng tôi!"
            );
        }
    }

    private ObjectNode buildOrderPayload(Order order, CurrencyType paymentCurrency, BigDecimal paymentAmount, String customerEmail) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("intent", "CAPTURE");

        ArrayNode purchaseUnits = root.putArray("purchase_units");
        ObjectNode purchaseUnit = purchaseUnits.addObject();
        purchaseUnit.put("reference_id", order.getId().toString());
        purchaseUnit.put("custom_id", order.getId().toString());

        ObjectNode amount = purchaseUnit.putObject("amount");
        amount.put("currency_code", paymentCurrency.name());
        amount.put("value", formatAmount(paymentAmount));

        ArrayNode itemsNode = purchaseUnit.putArray("items");
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            for (OrderItem item : order.getItems()) {
                BigDecimal itemAmount = BigDecimal.valueOf(item.getUnitPriceCents());
                if (order.getCurrency() == CurrencyType.VND && paymentCurrency == CurrencyType.USD) {
                    itemAmount = exchangeRateService.convertAmount(item.getUnitPriceCents(), CurrencyType.VND, CurrencyType.USD);
                }
                
                ObjectNode itemNode = itemsNode.addObject();
                itemNode.put("name", item.getTitle());
                itemNode.put("quantity", item.getQuantity());
                ObjectNode unitAmount = itemNode.putObject("unit_amount");
                unitAmount.put("currency_code", paymentCurrency.name());
                unitAmount.put("value", formatAmount(itemAmount));
            }

            ObjectNode breakdown = amount.putObject("breakdown");
            ObjectNode itemTotal = breakdown.putObject("item_total");
            itemTotal.put("currency_code", paymentCurrency.name());
            itemTotal.put("value", formatAmount(paymentAmount));
        }

        ObjectNode applicationContext = root.putObject("application_context");
        applicationContext.put("brand_name", Optional.ofNullable(payPalProperties.getBrandName()).orElse("English Platform"));
        applicationContext.put("landing_page", "NO_PREFERENCE");
        applicationContext.put("user_action", "PAY_NOW");
        applicationContext.put("return_url", appendOrderId(payPalProperties.getSuccessUrl(), order.getId()));
        applicationContext.put("cancel_url", appendOrderId(payPalProperties.getCancelUrl(), order.getId()));

        if (customerEmail != null && !customerEmail.isBlank()) {
            ObjectNode payer = root.putObject("payer");
            payer.put("email_address", customerEmail);
        }

        return root;
    }

    private Optional<String> extractLink(JsonNode response, String rel) {
        if (!response.has("links")) {
            return Optional.empty();
        }
        for (JsonNode link : response.path("links")) {
            if (rel.equalsIgnoreCase(link.path("rel").asText())) {
                return Optional.ofNullable(link.path("href").asText(null));
            }
        }
        return Optional.empty();
    }

    private String appendOrderId(String baseUrl, UUID orderId) {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new ResourceInvalidException("PayPal redirect URL is not configured");
        }
        String separator = baseUrl.contains("?") ? "&" : "?";
        return baseUrl + separator + "orderId=" + orderId;
    }

    // private String formatAmount(Long amountCents) {
    //     if (amountCents == null) {
    //         return "0.00";
    //     }
    //     // Use database value as-is (no division by 100)
    //     return BigDecimal.valueOf(amountCents)
    //             .setScale(2, RoundingMode.HALF_UP)
    //             .toPlainString();
    // }

    private void handlePayoutBatchDenied(PayPalWebhookRequest webhookEvent) {
        try {
            Map<String, Object> resource = webhookEvent.resource();
            if (resource.containsKey("batch_header")) {
                Map<String, Object> batchHeader = (Map<String, Object>) resource.get("batch_header");
                String payoutBatchId = (String) batchHeader.get("payout_batch_id");
                String batchStatus = (String) batchHeader.get("batch_status");
                
                if (payoutBatchId != null && "DENIED".equalsIgnoreCase(batchStatus)) {
                    withdrawalService.handlePayoutBatchFailed(payoutBatchId, "Payout batch denied by PayPal");
                }
            }
        } catch (Exception e) {
            // Log but don't fail - manual intervention may be needed
            throw new ResourceInvalidException("Failed to process PayPal payout denied webhook: " + e.getMessage());
        }
    }
    
    private void handlePayoutBatchSuccess(PayPalWebhookRequest webhookEvent) {
        try {
            Map<String, Object> resource = webhookEvent.resource();
            if (resource.containsKey("batch_header")) {
                Map<String, Object> batchHeader = (Map<String, Object>) resource.get("batch_header");
                String payoutBatchId = (String) batchHeader.get("payout_batch_id");
                String batchStatus = (String) batchHeader.get("batch_status");
                
                if (payoutBatchId != null && "SUCCESS".equalsIgnoreCase(batchStatus)) {
                    withdrawalService.handlePayoutBatchSuccess(payoutBatchId);
                }
            }
        } catch (Exception e) {
            // Log but don't fail - manual intervention may be needed
            throw new ResourceInvalidException("Failed to process PayPal payout success webhook: " + e.getMessage());
        }
    }
    
    private String formatAmount(BigDecimal amount) {
        if (amount == null) {
            return "0.00";
        }
        return amount.setScale(2, RoundingMode.HALF_UP)
                .toPlainString();
    }
}
