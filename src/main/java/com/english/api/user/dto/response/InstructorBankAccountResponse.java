package com.english.api.user.dto.response;

import com.english.api.user.model.InstructorBankAccount;

import java.time.Instant;
import java.util.UUID;

public record InstructorBankAccountResponse(
        UUID id,
        String paypalEmail,
        Instant createdAt,
        Instant updatedAt
) {
    public static InstructorBankAccountResponse from(InstructorBankAccount bankAccount) {
        return new InstructorBankAccountResponse(
                bankAccount.getId(),
                bankAccount.getPaypalEmail(),
                bankAccount.getCreatedAt(),
                bankAccount.getUpdatedAt()
        );
    }
}
