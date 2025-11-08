package com.english.api.order.service;

import com.english.api.order.model.Order;
import com.english.api.order.model.Payment;

public interface InvoiceService {
    void generateAndSendInvoiceAsync(Order order, Payment payment);
}
