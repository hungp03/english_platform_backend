package com.english.api.user.dto.response;

import java.math.BigDecimal;

public record InstructorBalanceResponse(
        BigDecimal availableBalanceCents,
        BigDecimal pendingBalanceCents,
        String availableBalanceFormatted,
        String pendingBalanceFormatted
) {
    public static InstructorBalanceResponse from(BigDecimal availableCents, BigDecimal pendingCents) {
        return new InstructorBalanceResponse(
                availableCents,
                pendingCents,
                formatCents(availableCents),
                formatCents(pendingCents)
        );
    }
    
    private static String formatCents(BigDecimal cents) {
        if (cents == null) return "0 VND";
        return String.format("%,.2f VND", cents);
    }
}
