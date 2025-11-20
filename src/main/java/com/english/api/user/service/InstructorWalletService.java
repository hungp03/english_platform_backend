package com.english.api.user.service;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.order.model.Order;
import com.english.api.user.dto.response.InstructorBalanceResponse;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.UUID;

public interface InstructorWalletService {
    
    /**
     * Get instructor's current balance
     */
    InstructorBalanceResponse getBalance();
    
    /**
     * Get instructor's transaction history
     */
    PaginationResponse getTransactions(Pageable pageable);
    
    /**
     * Process order earnings - credit instructors when order is paid
     * This should be called when order status changes to PAID
     */
    void processOrderEarnings(Order order);
    
    /**
     * Deduct amount from instructor balance (for withdrawal)
     */
    void deductBalance(UUID userId, BigDecimal amountCents, UUID referenceId, String description, String currency);
    
    /**
     * Refund amount to instructor balance (for rejected withdrawal)
     */
    void refundBalance(UUID userId, BigDecimal amountCents, UUID referenceId, String description, String currency);
    
    /**
     * Complete withdrawal - remove from pending balance
     */
    void completeWithdrawal(UUID userId, BigDecimal amountCents);
}
