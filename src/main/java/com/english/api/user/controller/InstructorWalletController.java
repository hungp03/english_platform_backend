package com.english.api.user.controller;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.user.dto.request.CreateWithdrawalRequest;
import com.english.api.user.dto.request.UpdateBankAccountRequest;
import com.english.api.user.dto.response.InstructorBalanceResponse;
import com.english.api.user.dto.response.InstructorBankAccountResponse;
import com.english.api.user.dto.response.WithdrawalRequestResponse;
import com.english.api.user.service.InstructorWalletService;
import com.english.api.user.service.WithdrawalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/instructor/wallet")
@RequiredArgsConstructor
@PreAuthorize("hasRole('INSTRUCTOR')")
public class InstructorWalletController {
    
    private final InstructorWalletService walletService;
    private final WithdrawalService withdrawalService;
    
    @GetMapping("/balance")
    public ResponseEntity<InstructorBalanceResponse> getBalance() {
        InstructorBalanceResponse balance = walletService.getBalance();
        return ResponseEntity.ok(balance);
    }
    
    @GetMapping("/transactions")
    public ResponseEntity<PaginationResponse> getTransactions(
            @PageableDefault(size = 20) Pageable pageable) {
        PaginationResponse transactions = walletService.getTransactions(pageable);
        return ResponseEntity.ok(transactions);
    }
    
    @PostMapping("/withdrawals")
    public ResponseEntity<WithdrawalRequestResponse> createWithdrawal(
            @Valid @RequestBody CreateWithdrawalRequest request) {
        WithdrawalRequestResponse response = withdrawalService.createWithdrawalRequest(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/withdrawals")
    public ResponseEntity<PaginationResponse> getWithdrawals(
            @PageableDefault(size = 20) Pageable pageable) {
        PaginationResponse withdrawals = withdrawalService.getWithdrawalRequests(pageable);
        return ResponseEntity.ok(withdrawals);
    }
    
    @DeleteMapping("/withdrawals/{id}")
    public ResponseEntity<WithdrawalRequestResponse> cancelWithdrawal(@PathVariable UUID id) {
        WithdrawalRequestResponse response = withdrawalService.cancelWithdrawalRequest(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/bank-account")
    public ResponseEntity<InstructorBankAccountResponse> getBankAccount() {
        InstructorBankAccountResponse bankAccount = withdrawalService.getBankAccount();
        return ResponseEntity.ok(bankAccount);
    }
    
    @PutMapping("/bank-account")
    public ResponseEntity<InstructorBankAccountResponse> updateBankAccount(
            @Valid @RequestBody UpdateBankAccountRequest request) {
        InstructorBankAccountResponse bankAccount = withdrawalService.updateBankAccount(request);
        return ResponseEntity.ok(bankAccount);
    }
    
    @DeleteMapping("/bank-account")
    public ResponseEntity<Void> deleteBankAccount() {
        withdrawalService.deleteBankAccount();
        return ResponseEntity.noContent().build();
    }
}
