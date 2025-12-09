package com.english.api.order.service;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.order.dto.request.ApplyVoucherDirectRequest;
import com.english.api.order.dto.request.ApplyVoucherRequest;
import com.english.api.order.dto.request.CreateVoucherRequest;
import com.english.api.order.dto.request.UpdateVoucherRequest;
import com.english.api.order.dto.response.VoucherApplyResponse;
import com.english.api.order.dto.response.VoucherResponse;
import com.english.api.order.model.enums.VoucherStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface InstructorVoucherService {

    /**
     * Create a new voucher for the current instructor
     */
    VoucherResponse createVoucher(CreateVoucherRequest request);

    /**
     * Update an existing voucher
     */
    VoucherResponse updateVoucher(UUID voucherId, UpdateVoucherRequest request);

    /**
     * Delete a voucher (soft delete by setting status to INACTIVE)
     */
    void deleteVoucher(UUID voucherId);

    /**
     * Get voucher by ID for the current instructor
     */
    VoucherResponse getVoucherById(UUID voucherId);

    /**
     * Get all vouchers for the current instructor with pagination
     */
    PaginationResponse getMyVouchers(VoucherStatus status, Pageable pageable);

    /**
     * Apply voucher to cart and calculate discount
     */
    VoucherApplyResponse applyVoucherToCart(ApplyVoucherRequest request);

    /**
     * Apply voucher to a single course directly
     */
    VoucherApplyResponse applyVoucherToCourse(ApplyVoucherDirectRequest request);

    /**
     * Apply voucher to specific course IDs (used when creating order)
     */
    VoucherApplyResponse applyVoucherToCourseIds(String code, List<UUID> courseIds);

    /**
     * Validate voucher code (public endpoint for checking)
     */
    VoucherApplyResponse validateVoucher(String code);
}
