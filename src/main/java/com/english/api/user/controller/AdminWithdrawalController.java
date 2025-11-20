package com.english.api.user.controller;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.user.dto.request.ProcessWithdrawalRequest;
import com.english.api.user.dto.response.WithdrawalRequestResponse;
import com.english.api.user.model.enums.WithdrawalStatus;
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
@RequestMapping("/api/admin/withdrawals")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminWithdrawalController {
    
    private final WithdrawalService withdrawalService;
    
    @GetMapping
    public ResponseEntity<PaginationResponse> getAllWithdrawals(
            @RequestParam(value = "state", required = false) WithdrawalStatus state,
            @PageableDefault(size = 20) Pageable pageable) {
        PaginationResponse withdrawals = 
                withdrawalService.getAllWithdrawalRequests(state, pageable);
        return ResponseEntity.ok(withdrawals);
    }
    
    @PutMapping("/{id}/process")
    public ResponseEntity<WithdrawalRequestResponse> processWithdrawal(
            @PathVariable UUID id,
            @Valid @RequestBody ProcessWithdrawalRequest request) {
        WithdrawalRequestResponse response = withdrawalService.processWithdrawalRequest(id, request);
        return ResponseEntity.ok(response);
    }
}
