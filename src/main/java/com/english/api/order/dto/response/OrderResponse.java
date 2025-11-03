package com.english.api.order.dto.response;

import com.english.api.order.model.enums.CurrencyType;
import com.english.api.order.model.enums.OrderStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        OrderStatus status,
        CurrencyType currency,
        Long totalCents,
        OffsetDateTime createdAt,
        OffsetDateTime paidAt,
        String cancelReason,
        OffsetDateTime cancelAt,
        List<OrderItemResponse> items
//        List<PaymentResponse> payments,
//        List<InvoiceResponse> invoices
) {}