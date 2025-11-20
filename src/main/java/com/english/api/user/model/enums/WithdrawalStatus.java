package com.english.api.user.model.enums;

public enum WithdrawalStatus {
    PENDING,    // Awaiting admin review
    APPROVED,   // Approved by admin, awaiting payout
    PROCESSING, // PayPal payout in progress
    COMPLETED,  // Payout successful
    REJECTED,   // Rejected by admin
    FAILED      // Payout failed
}
