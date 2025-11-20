package com.english.api.user.dto.response;

import com.english.api.user.model.enums.WithdrawalStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record WithdrawalRequestResponse(
        UUID id,
        UUID userId,
        String instructorName,
        String instructorEmail,
        BigDecimal amountCents,
        String amountFormatted,
        BigDecimal originalAmountCents,
        String originalCurrency,
        String originalAmountFormatted,
        BigDecimal exchangeRate,
        WithdrawalStatus status,
        String bankInfoJson,
        String adminNote,
        String paypalPayoutBatchId,
        String paypalPayoutItemId,
        Instant processedAt,
        Instant completedAt,
        Instant createdAt
) {}
