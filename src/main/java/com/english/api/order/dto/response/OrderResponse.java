package com.english.api.order.dto.response;

import com.english.api.order.model.enums.CurrencyType;
import com.english.api.order.model.enums.OrderStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        UUID userId,
        OrderStatus status,
        CurrencyType currency,
        Long totalCents,
        OffsetDateTime createdAt,
        OffsetDateTime paidAt,
        List<OrderItemResponse> items
//        List<PaymentResponse> payments,
//        List<InvoiceResponse> invoices
) {}