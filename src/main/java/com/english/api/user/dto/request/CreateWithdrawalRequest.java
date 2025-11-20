package com.english.api.user.dto.request;

import com.english.api.order.model.enums.CurrencyType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateWithdrawalRequest(
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        BigDecimal amountCents,
        
        @NotNull(message = "Currency is required")
        CurrencyType currency
) {}
