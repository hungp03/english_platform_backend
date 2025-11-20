package com.english.api.user.service;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.user.dto.request.CreateWithdrawalRequest;
import com.english.api.user.dto.request.ProcessWithdrawalRequest;
import com.english.api.user.dto.request.UpdateBankAccountRequest;
import com.english.api.user.dto.response.InstructorBankAccountResponse;
import com.english.api.user.dto.response.WithdrawalRequestResponse;
import com.english.api.user.model.enums.WithdrawalStatus;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface WithdrawalService {
    
    /**
     * Create a new withdrawal request for instructor
     */
    WithdrawalRequestResponse createWithdrawalRequest(CreateWithdrawalRequest request);
    
    /**
     * Get withdrawal requests for an instructor
     */
    PaginationResponse getWithdrawalRequests(Pageable pageable);
    
    /**
     * Get all withdrawal requests (admin)
     */
    PaginationResponse getAllWithdrawalRequests(WithdrawalStatus status, Pageable pageable);
    
    /**
     * Process withdrawal request (approve/reject by admin)
     */
    WithdrawalRequestResponse processWithdrawalRequest(UUID requestId, ProcessWithdrawalRequest request);
    
    /**
     * Get or create bank account for instructor
     */
    InstructorBankAccountResponse getBankAccount();
    
    /**
     * Update bank account details
     */
    InstructorBankAccountResponse updateBankAccount(UpdateBankAccountRequest request);
    
    /**
     * Delete bank account for instructor
     */
    void deleteBankAccount();
    
    /**
     * Cancel/revoke a pending withdrawal request (instructor only)
     */
    WithdrawalRequestResponse cancelWithdrawalRequest(UUID requestId);
    
    /**
     * Handle PayPal payout batch failure (webhook)
     */
    void handlePayoutBatchFailed(String payoutBatchId, String reason);
    
    /**
     * Handle PayPal payout batch success (webhook)
     */
    void handlePayoutBatchSuccess(String payoutBatchId);
}
