package com.english.api.order.service;

import com.english.api.order.dto.response.InvoiceResponse;
import com.english.api.order.model.Order;
import com.english.api.order.model.Payment;

import java.util.UUID;

public interface InvoiceService {
    void generateAndSendInvoiceAsync(Order order, Payment payment);
    InvoiceResponse getInvoiceByOrderId(UUID orderId);
}
