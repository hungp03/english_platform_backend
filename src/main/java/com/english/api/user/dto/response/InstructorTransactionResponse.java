package com.english.api.user.dto.response;

import com.english.api.user.model.enums.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record InstructorTransactionResponse(
        UUID id,
        TransactionType type,
        BigDecimal amountCents,
        String currency,
        String amountFormatted,
        BigDecimal balanceAfterCents,
        UUID referenceId,
        String description,
        Instant createdAt
) {
}
