package com.english.api.user.service.impl;

import com.english.api.auth.util.SecurityUtil;
import com.english.api.common.dto.PaginationResponse;
import com.english.api.common.exception.ResourceInvalidException;
import com.english.api.common.exception.ResourceNotFoundException;
import com.english.api.order.model.enums.CurrencyType;
import com.english.api.order.service.ExchangeRateService;
import com.english.api.order.service.paypal.PayPalPayoutService;
import com.english.api.user.dto.request.CreateWithdrawalRequest;
import com.english.api.user.dto.request.ProcessWithdrawalRequest;
import com.english.api.user.dto.request.UpdateBankAccountRequest;
import com.english.api.user.dto.response.InstructorBankAccountResponse;
import com.english.api.user.dto.response.WithdrawalRequestResponse;
import com.english.api.user.model.InstructorBankAccount;
import com.english.api.user.model.User;
import com.english.api.user.model.WithdrawalRequest;
import com.english.api.user.model.enums.WithdrawalStatus;
import com.english.api.user.repository.InstructorBankAccountRepository;
import com.english.api.user.repository.WithdrawalRequestRepository;
import com.english.api.user.repository.UserRepository;
import com.english.api.user.service.InstructorWalletService;
import com.english.api.user.service.WithdrawalService;
import com.english.api.notification.service.NotificationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WithdrawalServiceImpl implements WithdrawalService {
    
    private final WithdrawalRequestRepository withdrawalRepository;
    private final InstructorBankAccountRepository bankAccountRepository;
    private final InstructorWalletService walletService;
    private final UserRepository userRepository;
    private final PayPalPayoutService payoutService;
    private final ObjectMapper objectMapper;
    private final ExchangeRateService exchangeRateService;
    private final NotificationService notificationService;
    
    @Value("${app.minimum-withdrawal-usd:10}")
    private BigDecimal minimumWithdrawalUsd;
    
    @Value("${app.minimum-withdrawal-vnd:250000}")
    private BigDecimal minimumWithdrawalVnd;
    
    @Override
    @Transactional
    public WithdrawalRequestResponse createWithdrawalRequest(CreateWithdrawalRequest request) {
        UUID userId = SecurityUtil.getCurrentUserId();
        BigDecimal originalAmountCents = request.amountCents();
        CurrencyType originalCurrency = request.currency();
        BigDecimal amountUsd;
        BigDecimal exchangeRate = null;
        
        // Convert to USD if needed
        if (originalCurrency == CurrencyType.VND) {
            // Validate minimum VND amount
            if (originalAmountCents.compareTo(minimumWithdrawalVnd) < 0) {
                throw new ResourceInvalidException(
                        String.format("Minimum withdrawal amount is %,.2f VND", minimumWithdrawalVnd));
            }
            
            // Convert VND to USD
            exchangeRate = exchangeRateService.getExchangeRate(
                    com.english.api.order.model.enums.CurrencyType.VND, 
                    com.english.api.order.model.enums.CurrencyType.USD);
            amountUsd = exchangeRateService.convertAmount(
                    originalAmountCents.longValue(),
                    com.english.api.order.model.enums.CurrencyType.VND,
                    com.english.api.order.model.enums.CurrencyType.USD);
            
            log.info("Converting VND {} to USD {} at rate {}", originalAmountCents, amountUsd, exchangeRate);
        } else {
            // Already USD
            amountUsd = originalAmountCents;
            
            // Validate minimum USD amount
            if (amountUsd.compareTo(minimumWithdrawalUsd) < 0) {
                throw new ResourceInvalidException(
                        String.format("Minimum withdrawal amount is $%.2f", minimumWithdrawalUsd));
            }
        }
        
        // Check if bank account exists
        InstructorBankAccount bankAccount = bankAccountRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceInvalidException(
                        "Please set up your bank account details before requesting withdrawal"));
        
        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Ensure amount is scaled to 2 decimal places without rounding up
        amountUsd = amountUsd.setScale(2, RoundingMode.DOWN);
        originalAmountCents = originalAmountCents.setScale(2, RoundingMode.DOWN);
        
        // Create bank info snapshot
        String bankInfoJson;
        try {
            Map<String, String> bankInfo = new HashMap<>();
            bankInfo.put("paypalEmail", bankAccount.getPaypalEmail());
            bankInfoJson = objectMapper.writeValueAsString(bankInfo);
        } catch (JsonProcessingException e) {
            bankInfoJson = "{}";
        }
        
        // Create withdrawal request
        WithdrawalRequest withdrawal = WithdrawalRequest.builder()
                .user(user)
                .amountCents(amountUsd)
                .originalAmountCents(originalAmountCents)
                .originalCurrency(originalCurrency.name())
                .exchangeRate(exchangeRate)
                .status(WithdrawalStatus.PENDING)
                .bankInfoJson(bankInfoJson)
                .build();
        
        withdrawal = withdrawalRepository.save(withdrawal);
        
        // Deduct balance in the original currency (not the converted USD amount)
        walletService.deductBalance(userId, originalAmountCents, withdrawal.getId(), 
                "Yêu cầu rút tiền #" + withdrawal.getId(), originalCurrency.name());
        
        log.info("Withdrawal request created: userId={}, originalAmount={} {}, usdAmount={}, requestId={}", 
                userId, originalAmountCents, originalCurrency, amountUsd, withdrawal.getId());
        
        return mapToResponse(withdrawal);
    }
    
    @Override
    public PaginationResponse getWithdrawalRequests(Pageable pageable) {
        UUID userId = SecurityUtil.getCurrentUserId();
        Page<WithdrawalRequestResponse> page = withdrawalRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToResponse);
        return PaginationResponse.from(page, pageable);
    }
    
    @Override
    public PaginationResponse getAllWithdrawalRequests(WithdrawalStatus status, Pageable pageable) {
        Page<WithdrawalRequestResponse> page;
        if (status != null) {
            page = withdrawalRepository.findByStatusOrderByCreatedAtAsc(status, pageable)
                    .map(this::mapToResponse);
        } else {
            page = withdrawalRepository.findAll(pageable)
                    .map(this::mapToResponse);
        }
        return PaginationResponse.from(page, pageable);
    }
    
    @Override
    @Transactional
    public WithdrawalRequestResponse processWithdrawalRequest(UUID requestId, ProcessWithdrawalRequest request) {
        WithdrawalRequest withdrawal = withdrawalRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Withdrawal request not found"));
        
        if (withdrawal.getStatus() != WithdrawalStatus.PENDING && 
            withdrawal.getStatus() != WithdrawalStatus.APPROVED) {
            throw new ResourceInvalidException("Withdrawal request cannot be modified in current status");
        }
        
        switch (request.status()) {
            case APPROVED -> approveWithdrawal(withdrawal, request.adminNote());
            case REJECTED -> rejectWithdrawal(withdrawal, request.adminNote());
            case COMPLETED -> completeWithdrawal(withdrawal, request.adminNote());
            default -> throw new ResourceInvalidException("Invalid status transition");
        }
        
        return mapToResponse(withdrawal);
    }
    
    private void approveWithdrawal(WithdrawalRequest withdrawal, String adminNote) {
        // Get bank account for PayPal email
        InstructorBankAccount bankAccount = bankAccountRepository.findByUserId(withdrawal.getUser().getId())
                .orElseThrow(() -> new ResourceInvalidException("Bank account not found"));
        
        try {
            // Create PayPal payout - use the exact amount without rounding
            BigDecimal amountUSD = withdrawal.getAmountCents()
                    .setScale(2, RoundingMode.DOWN);
            
            PayPalPayoutService.PayoutResult result = payoutService.createPayout(
                    bankAccount.getPaypalEmail(),
                    amountUSD,
                    withdrawal.getId().toString(),
                    "Instructor earnings withdrawal"
            );
            
            withdrawal.setStatus(WithdrawalStatus.PROCESSING);
            withdrawal.setPaypalPayoutBatchId(result.batchId());
            withdrawal.setPaypalPayoutItemId(result.payoutItemId());
            withdrawal.setProcessedAt(Instant.now());
            withdrawal.setAdminNote(adminNote);
            
            withdrawalRepository.save(withdrawal);
            
            log.info("Withdrawal approved and payout initiated: requestId={}, batchId={}", 
                    withdrawal.getId(), result.batchId());
                    
        } catch (Exception e) {
            log.error("Failed to create PayPal payout for withdrawal: " + withdrawal.getId(), e);
            throw new ResourceInvalidException("Failed to initiate payout: " + e.getMessage());
        }
    }
    
    private void rejectWithdrawal(WithdrawalRequest withdrawal, String adminNote) {
        // Check if already rejected (idempotency)
        if (withdrawal.getStatus() == WithdrawalStatus.REJECTED) {
            log.warn("Withdrawal already rejected: requestId={}", withdrawal.getId());
            throw new ResourceInvalidException("Withdrawal request has already been rejected");
        }
        
        // Update status FIRST to prevent race condition
        withdrawal.setStatus(WithdrawalStatus.REJECTED);
        withdrawal.setAdminNote(adminNote);
        withdrawal.setProcessedAt(Instant.now());
        withdrawalRepository.save(withdrawal);
        
        // Refund the amount back to instructor's balance in original currency (after status update)
        String currency = withdrawal.getOriginalCurrency() != null ? 
                withdrawal.getOriginalCurrency() : "USD";
        walletService.refundBalance(
                withdrawal.getUser().getId(),
                withdrawal.getOriginalAmountCents(),
                withdrawal.getId(),
                "Yêu cầu rút tiền bị từ chối: " + (adminNote != null ? adminNote : "Không có lý do nào được cung cấp"),
                currency
        );
        
        log.info("Withdrawal rejected: requestId={}, amount refunded", withdrawal.getId());
    }
    
    private void completeWithdrawal(WithdrawalRequest withdrawal, String adminNote) {
        if (withdrawal.getStatus() == WithdrawalStatus.COMPLETED) {
            log.warn("Withdrawal already completed: requestId={}", withdrawal.getId());
            throw new ResourceInvalidException("Withdrawal request has already been completed");
        }
        
        if (withdrawal.getStatus() != WithdrawalStatus.PROCESSING) {
            throw new ResourceInvalidException("Can only mark PROCESSING withdrawals as completed");
        }
        
        // Update status FIRST to prevent race condition with webhook
        withdrawal.setStatus(WithdrawalStatus.COMPLETED);
        withdrawal.setCompletedAt(Instant.now());
        if (adminNote != null) {
            withdrawal.setAdminNote(adminNote);
        }
        withdrawalRepository.save(withdrawal);
        
        // Remove from pending balance (after status update to ensure idempotency)
        walletService.completeWithdrawal(
                withdrawal.getUser().getId(),
                withdrawal.getOriginalAmountCents()
        );
        
        log.info("Withdrawal marked as completed: requestId={}", withdrawal.getId());
    }
    
    @Override
    public InstructorBankAccountResponse getBankAccount() {
        UUID userId = SecurityUtil.getCurrentUserId();
        InstructorBankAccount bankAccount = bankAccountRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                    return InstructorBankAccount.builder()
                            .user(user)
                            .paypalEmail("")
                            .build();
                });
        return InstructorBankAccountResponse.from(bankAccount);
    }
    
    @Override
    @Transactional
    public InstructorBankAccountResponse updateBankAccount(UpdateBankAccountRequest request) {
        UUID userId = SecurityUtil.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        InstructorBankAccount bankAccount = bankAccountRepository.findByUserId(userId)
                .orElseGet(() -> InstructorBankAccount.builder()
                        .user(user)
                        .build());
        
        bankAccount.setPaypalEmail(request.paypalEmail());
        
        InstructorBankAccount saved = bankAccountRepository.save(bankAccount);
        return InstructorBankAccountResponse.from(saved);
    }
    
    @Override
    @Transactional
    public void deleteBankAccount() {
        UUID userId = SecurityUtil.getCurrentUserId();
        bankAccountRepository.findByUserId(userId)
                .ifPresent(bankAccountRepository::delete);
    }
    
    private WithdrawalRequestResponse mapToResponse(WithdrawalRequest withdrawal) {
        String originalCurrency = withdrawal.getOriginalCurrency() != null ? 
                withdrawal.getOriginalCurrency() : "USD";
        
        return new WithdrawalRequestResponse(
                withdrawal.getId(),
                withdrawal.getUser().getId(),
                withdrawal.getUser().getFullName(),
                withdrawal.getUser().getEmail(),
                withdrawal.getAmountCents(),
                formatAmount(withdrawal.getAmountCents(), "USD"),
                withdrawal.getOriginalAmountCents(),
                originalCurrency,
                formatAmount(withdrawal.getOriginalAmountCents(), originalCurrency),
                withdrawal.getExchangeRate(),
                withdrawal.getStatus(),
                withdrawal.getBankInfoJson(),
                withdrawal.getAdminNote(),
                withdrawal.getPaypalPayoutBatchId(),
                withdrawal.getPaypalPayoutItemId(),
                withdrawal.getProcessedAt(),
                withdrawal.getCompletedAt(),
                withdrawal.getCreatedAt()
        );
    }
    
    @Override
    @Transactional
    public WithdrawalRequestResponse cancelWithdrawalRequest(UUID requestId) {
        UUID userId = SecurityUtil.getCurrentUserId();
        WithdrawalRequest withdrawal = withdrawalRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Withdrawal request not found"));
        
        // Verify ownership
        if (!withdrawal.getUser().getId().equals(userId)) {
            throw new ResourceInvalidException("You can only cancel your own withdrawal requests");
        }
        
        // Only allow cancellation of pending requests
        if (withdrawal.getStatus() != WithdrawalStatus.PENDING) {
            throw new ResourceInvalidException("Only pending withdrawal requests can be cancelled");
        }
        
        // Update status FIRST to prevent race condition (double cancel)
        withdrawal.setStatus(WithdrawalStatus.REJECTED);
        withdrawal.setAdminNote("Bị hủy bởi người dùng");
        withdrawal.setProcessedAt(Instant.now());
        withdrawalRepository.save(withdrawal);
        
        // Refund the amount back to instructor's balance in original currency (after status update)
        String currency = withdrawal.getOriginalCurrency() != null ? 
                withdrawal.getOriginalCurrency() : "USD";
        walletService.refundBalance(
                userId,
                withdrawal.getOriginalAmountCents(),
                withdrawal.getId(),
                "Yêu cầu rút tiền đã bị giảng viên hủy bỏ",
                currency
        );
        
        log.info("Withdrawal cancelled by instructor: requestId={}, userId={}", requestId, userId);
        
        return mapToResponse(withdrawal);
    }
    
    @Override
    @Transactional
    public void handlePayoutBatchFailed(String payoutBatchId, String reason) {
        withdrawalRepository.findByPaypalPayoutBatchId(payoutBatchId).ifPresent(withdrawal -> {
            // Skip if already failed/rejected (idempotency check)
            if (withdrawal.getStatus() == WithdrawalStatus.FAILED || 
                withdrawal.getStatus() == WithdrawalStatus.REJECTED) {
                log.info("PayPal payout already failed/rejected, skipping: batchId={}, requestId={}", 
                        payoutBatchId, withdrawal.getId());
                return;
            }
            
            if (withdrawal.getStatus() == WithdrawalStatus.PROCESSING) {
                // Update status FIRST to prevent race condition
                withdrawal.setStatus(WithdrawalStatus.FAILED);
                withdrawal.setAdminNote("PayPal payout denied/failed: " + (reason != null ? reason : "Unknown error"));
                withdrawalRepository.save(withdrawal);
                
                // Refund the amount back to instructor's balance (after status update)
                String currency = withdrawal.getOriginalCurrency() != null ? 
                        withdrawal.getOriginalCurrency() : "USD";
                walletService.refundBalance(
                        withdrawal.getUser().getId(),
                        withdrawal.getOriginalAmountCents(),
                        withdrawal.getId(),
                        "PayPal payout failed: " + (reason != null ? reason : "Unknown error"),
                        currency
                );
                
                // Notify user about failed payout
                notificationService.sendNotification(
                    withdrawal.getUser().getId(),
                    "Rút tiền thất bại",
                    "Yêu cầu rút tiền #" + withdrawal.getId() + " đã bị từ chối bởi PayPal. Số tiền đã được hoàn trả vào ví của bạn. Vui lòng kiểm tra thông tin tài khoản PayPal và thử lại."
                );
                
                log.warn("PayPal payout batch failed: batchId={}, requestId={}, reason={}", 
                        payoutBatchId, withdrawal.getId(), reason);
            }
        });
    }
    
    @Override
    @Transactional
    public void handlePayoutBatchSuccess(String payoutBatchId) {
        withdrawalRepository.findByPaypalPayoutBatchId(payoutBatchId).ifPresent(withdrawal -> {
            // Skip if already completed (idempotency check)
            if (withdrawal.getStatus() == WithdrawalStatus.COMPLETED) {
                log.info("PayPal payout already completed, skipping: batchId={}, requestId={}", 
                        payoutBatchId, withdrawal.getId());
                return;
            }
            
            if (withdrawal.getStatus() == WithdrawalStatus.PROCESSING) {
                // Update status FIRST to prevent race condition with admin completion
                withdrawal.setStatus(WithdrawalStatus.COMPLETED);
                withdrawal.setCompletedAt(Instant.now());
                if (withdrawal.getAdminNote() == null) {
                    withdrawal.setAdminNote("Thanh toán PayPal đã hoàn tất");
                }
                withdrawalRepository.save(withdrawal);
                
                // Remove from pending balance (after status update to ensure idempotency)
                walletService.completeWithdrawal(
                        withdrawal.getUser().getId(),
                        withdrawal.getOriginalAmountCents()
                );
                
                // Notify user about successful payout
                String formattedAmount = formatAmount(withdrawal.getOriginalAmountCents(), 
                        withdrawal.getOriginalCurrency() != null ? withdrawal.getOriginalCurrency() : "USD");
                notificationService.sendNotification(
                    withdrawal.getUser().getId(),
                    "Rút tiền thành công",
                    "Yêu cầu rút tiền #" + withdrawal.getId() + " đã được xử lý thành công. Số tiền " + formattedAmount + " đã được chuyển vào tài khoản PayPal của bạn."
                );
                
                log.info("PayPal payout batch succeeded: batchId={}, requestId={}", 
                        payoutBatchId, withdrawal.getId());
            }
        });
    }
    
    private String formatAmount(BigDecimal amount, String currency) {
        if (amount == null) return "0.00";
        if ("VND".equals(currency)) {
            return String.format("%,.2f VND", amount);
        }
        return String.format("$%.2f", amount);
    }
}
