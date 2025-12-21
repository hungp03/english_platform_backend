package com.english.api.order.controller;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.order.dto.request.ApplyVoucherDirectRequest;
import com.english.api.order.dto.request.ApplyVoucherRequest;
import com.english.api.order.dto.request.CreateVoucherRequest;
import com.english.api.order.dto.request.UpdateVoucherRequest;
import com.english.api.order.dto.response.VoucherApplyResponse;
import com.english.api.order.dto.response.VoucherResponse;
import com.english.api.order.model.enums.VoucherStatus;
import com.english.api.order.service.InstructorVoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class InstructorVoucherController {

    private final InstructorVoucherService voucherService;

    /**
     * Create a new voucher (Instructor only)
     */
    @PostMapping("/vouchers/instructor")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<VoucherResponse> createVoucher(@Valid @RequestBody CreateVoucherRequest request) {
        VoucherResponse response = voucherService.createVoucher(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update a voucher (Instructor only)
     */
    @PutMapping("/vouchers/instructor/{voucherId}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<VoucherResponse> updateVoucher(
            @PathVariable UUID voucherId,
            @Valid @RequestBody UpdateVoucherRequest request) {
        VoucherResponse response = voucherService.updateVoucher(voucherId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a voucher (Instructor only)
     */
    @DeleteMapping("/vouchers/instructor/{voucherId}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<Void> deleteVoucher(@PathVariable UUID voucherId) {
        voucherService.deleteVoucher(voucherId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get voucher by ID (Instructor only)
     */
    @GetMapping("/vouchers/instructor/{voucherId}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<VoucherResponse> getVoucherById(@PathVariable UUID voucherId) {
        VoucherResponse response = voucherService.getVoucherById(voucherId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all vouchers for the current instructor with pagination
     */
    @GetMapping("/vouchers/instructor")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<PaginationResponse> getMyVouchers(
            @RequestParam(required = false) VoucherStatus status,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        PaginationResponse response = voucherService.getMyVouchers(status, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Apply voucher to cart (User endpoint)
     */
    @PostMapping("/vouchers/apply")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<VoucherApplyResponse> applyVoucher(@Valid @RequestBody ApplyVoucherRequest request) {
        VoucherApplyResponse response = voucherService.applyVoucherToCart(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Apply voucher to a single course directly (User endpoint)
     */
    @PostMapping("/vouchers/apply-direct")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<VoucherApplyResponse> applyVoucherDirect(@Valid @RequestBody ApplyVoucherDirectRequest request) {
        VoucherApplyResponse response = voucherService.applyVoucherToCourse(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Validate voucher code (User endpoint)
     */
    @GetMapping("/vouchers/validate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<VoucherApplyResponse> validateVoucher(@RequestParam String code) {
        VoucherApplyResponse response = voucherService.validateVoucher(code);
        return ResponseEntity.ok(response);
    }

    /**
     * Get valid vouchers for a specific course (Public endpoint)
     */
    @GetMapping("/courses/{courseId}/vouchers")
    public ResponseEntity<PaginationResponse> getValidVouchersForCourse(
            @PathVariable UUID courseId,
            @PageableDefault(size = 5, sort = "discountValue", direction = Sort.Direction.DESC) Pageable pageable) {
        PaginationResponse response = voucherService.getValidVouchersForCourse(courseId, pageable);
        return ResponseEntity.ok(response);
    }
}
