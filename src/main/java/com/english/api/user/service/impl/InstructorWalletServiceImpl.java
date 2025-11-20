package com.english.api.user.service.impl;

import com.english.api.auth.util.SecurityUtil;
import com.english.api.common.dto.PaginationResponse;
import com.english.api.common.exception.ResourceInvalidException;
import com.english.api.course.model.Course;
import com.english.api.course.repository.CourseRepository;
import com.english.api.order.model.Order;
import com.english.api.order.model.OrderItem;
import com.english.api.order.model.enums.OrderItemEntityType;
import com.english.api.user.dto.response.InstructorBalanceResponse;
import com.english.api.user.dto.response.InstructorTransactionResponse;
import com.english.api.user.model.InstructorBalance;
import com.english.api.user.model.InstructorTransaction;
import com.english.api.user.model.User;
import com.english.api.user.model.enums.TransactionType;
import com.english.api.user.repository.InstructorBalanceRepository;
import com.english.api.user.repository.InstructorTransactionRepository;
import com.english.api.user.service.InstructorWalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InstructorWalletServiceImpl implements InstructorWalletService {
    
    private final InstructorBalanceRepository balanceRepository;
    private final InstructorTransactionRepository transactionRepository;
    private final CourseRepository courseRepository;
    
    @Value("${app.platform-fee-percentage:30}")
    private int platformFeePercentage;
    
    @Override
    public InstructorBalanceResponse getBalance() {
        UUID userId = SecurityUtil.getCurrentUserId();
        InstructorBalance balance = balanceRepository.findByUserId(userId)
                .orElse(InstructorBalance.builder()
                        .availableBalanceCents(BigDecimal.ZERO)
                        .pendingBalanceCents(BigDecimal.ZERO)
                        .build());
        
        return InstructorBalanceResponse.from(
                balance.getAvailableBalanceCents(),
                balance.getPendingBalanceCents()
        );
    }
    
    @Override
    public PaginationResponse getTransactions(Pageable pageable) {
        UUID userId = SecurityUtil.getCurrentUserId();
        Page<InstructorTransactionResponse> page = transactionRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(tx -> new InstructorTransactionResponse(
                        tx.getId(),
                        tx.getType(),
                        tx.getAmountCents(),
                        tx.getCurrency(),
                        formatCents(tx.getAmountCents(), tx.getCurrency()),
                        tx.getBalanceAfterCents(),
                        tx.getReferenceId(),
                        tx.getDescription(),
                        tx.getCreatedAt()
                ));
        return PaginationResponse.from(page, pageable);
    }
    
    @Override
    @Transactional
    public void processOrderEarnings(Order order) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            return;
        }
        
        for (OrderItem item : order.getItems()) {
            if (item.getEntity() == OrderItemEntityType.COURSE) {
                processCourseEarning(item, order);
            }
        }
    }
    
    private void processCourseEarning(OrderItem item, Order order) {
        Course course = courseRepository.findById(item.getEntityId())
                .orElse(null);
        
        if (course == null || course.getCreatedBy() == null) {
            log.warn("Course or instructor not found for OrderItem: {}", item.getId());
            return;
        }
        
        User instructor = course.getCreatedBy();
        BigDecimal itemPriceCents = BigDecimal.valueOf(item.getUnitPriceCents()).multiply(BigDecimal.valueOf(item.getQuantity()));
        
        // Calculate instructor share (after platform fee)
        BigDecimal instructorShareCents = itemPriceCents
                .multiply(BigDecimal.valueOf(100 - platformFeePercentage))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        
        // Get or create instructor balance
        InstructorBalance balance = balanceRepository.findByUserId(instructor.getId())
                .orElseGet(() -> {
                    InstructorBalance newBalance = InstructorBalance.builder()
                            .user(instructor)
                            .availableBalanceCents(BigDecimal.ZERO)
                            .pendingBalanceCents(BigDecimal.ZERO)
                            .build();
                    return balanceRepository.save(newBalance);
                });
        
        // Update balance
        balance.setAvailableBalanceCents(balance.getAvailableBalanceCents().add(instructorShareCents));
        balanceRepository.save(balance);
        
        // Create transaction record
        InstructorTransaction transaction = InstructorTransaction.builder()
                .user(instructor)
                .type(TransactionType.SALE)
                .amountCents(instructorShareCents)
                .currency("VND")
                .balanceAfterCents(balance.getAvailableBalanceCents())
                .referenceId(item.getId())
                .description(String.format("Earnings from course: %s (Order: %s)", 
                        course.getTitle(), order.getId()))
                .build();
        transactionRepository.save(transaction);
        
        log.info("Credited instructor {} with {} for course sale", 
                instructor.getId(), instructorShareCents);
    }
    
    @Override
    @Transactional
    public void deductBalance(UUID userId, BigDecimal amountCents, UUID referenceId, String description, String currency) {
        InstructorBalance balance = balanceRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceInvalidException("Instructor balance not found"));
        
        if (balance.getAvailableBalanceCents().compareTo(amountCents) < 0) {
            throw new ResourceInvalidException("Insufficient balance");
        }
        
        // Move from available to pending
        balance.setAvailableBalanceCents(balance.getAvailableBalanceCents().subtract(amountCents));
        balance.setPendingBalanceCents(balance.getPendingBalanceCents().add(amountCents));
        balanceRepository.save(balance);
        
        // Create transaction record
        InstructorTransaction transaction = InstructorTransaction.builder()
                .user(balance.getUser())
                .type(TransactionType.WITHDRAWAL)
                .amountCents(amountCents.negate())
                .currency(currency)
                .balanceAfterCents(balance.getAvailableBalanceCents())
                .referenceId(referenceId)
                .description(description)
                .build();
        transactionRepository.save(transaction);
    }
    
    @Override
    @Transactional
    public void refundBalance(UUID userId, BigDecimal amountCents, UUID referenceId, String description, String currency) {
        InstructorBalance balance = balanceRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceInvalidException("Instructor balance not found"));
        
        // Move from pending back to available
        balance.setPendingBalanceCents(balance.getPendingBalanceCents().subtract(amountCents));
        balance.setAvailableBalanceCents(balance.getAvailableBalanceCents().add(amountCents));
        balanceRepository.save(balance);
        
        // Create transaction record
        InstructorTransaction transaction = InstructorTransaction.builder()
                .user(balance.getUser())
                .type(TransactionType.WITHDRAWAL_REFUND)
                .amountCents(amountCents)
                .currency(currency)
                .balanceAfterCents(balance.getAvailableBalanceCents())
                .referenceId(referenceId)
                .description(description)
                .build();
        transactionRepository.save(transaction);
    }
    
    @Override
    @Transactional
    public void completeWithdrawal(UUID userId, BigDecimal amountCents) {
        InstructorBalance balance = balanceRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceInvalidException("Instructor balance not found"));
        
        // Remove from pending balance (already removed from available when request was created)
        balance.setPendingBalanceCents(balance.getPendingBalanceCents().subtract(amountCents));
        balanceRepository.save(balance);
    }
    
    private String formatCents(BigDecimal cents, String currency) {
        if (cents == null) return "0.00";
        String sign = cents.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "";
        if ("VND".equals(currency)) {
            return String.format("%s%,.2f VND", sign, cents);
        }
        return String.format("%s$%,.2f", sign, cents);
    }
    
    // private String formatBalance(BigDecimal cents, String currency) {
    //     if (cents == null) return "0.00";
    //     if ("VND".equals(currency)) {
    //         return String.format("%,.2f VND", cents);
    //     }
    //     return String.format("$%,.2f", cents);
    // }
}
