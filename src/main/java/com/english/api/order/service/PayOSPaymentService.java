package com.english.api.order.service;

import org.springframework.transaction.annotation.Transactional;
import vn.payos.type.CheckoutResponseData;

import java.util.UUID;

/**
 * Service interface for PayOS payment operations
 * Created by hungpham on 10/24/2025
 */
public interface PayOSPaymentService {
    // Tạo link thanh toán
    @Transactional
    CheckoutResponseData createPaymentLink(UUID orderId);

    // Xử lý webhook callback
    @Transactional
    void handleWebhook(vn.payos.type.Webhook webhookBody);

    @Transactional
    void cancelPayment(UUID orderId);
}