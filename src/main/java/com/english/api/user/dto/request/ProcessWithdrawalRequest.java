package com.english.api.user.dto.request;

import com.english.api.user.model.enums.WithdrawalStatus;
import jakarta.validation.constraints.NotNull;

public record ProcessWithdrawalRequest(
        @NotNull(message = "Status is required")
        WithdrawalStatus status, // APPROVED, REJECTED, or COMPLETED
        
        String adminNote
) {}
