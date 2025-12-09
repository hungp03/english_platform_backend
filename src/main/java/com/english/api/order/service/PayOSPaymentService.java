package com.english.api.order.service;

import org.springframework.transaction.annotation.Transactional;
import vn.payos.model.webhooks.Webhook;

import java.util.UUID;

/**
 * Service interface for PayOS payment operations
 * Created by hungpham on 10/24/2025
 */
public interface PayOSPaymentService {
    // Tạo link thanh toán
    @Transactional
    Object createPaymentLink(UUID orderId);

    // Xử lý webhook callback
    @Transactional
    void handleWebhook(Webhook webhookBody);
}