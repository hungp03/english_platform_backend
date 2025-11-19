package com.english.api.order.dto.response;

import com.english.api.order.model.enums.CurrencyType;

import java.time.OffsetDateTime;
import java.util.UUID;

public record InvoiceResponse(
        UUID id,
        UUID orderId,
        String number,
        Long totalCents,
        CurrencyType currency,
        String fileUrl,
        OffsetDateTime createdAt
) {}
