package com.english.api.user.model.enums;

public enum TransactionType {
    SALE,              // Earnings from course sale
    REFUND,            // Deduction due to refund
    WITHDRAWAL,        // Withdrawal request deduction
    WITHDRAWAL_REFUND, // Refund of rejected withdrawal
    ADJUSTMENT,        // Manual admin adjustment
    PLATFORM_FEE       // Platform fee deduction
}
