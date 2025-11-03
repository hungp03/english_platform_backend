package com.english.api.enrollment.controller;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.enrollment.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for Enrollment operations
 * Created by hungpham on 10/29/2025
 */
@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {
    private final EnrollmentService enrollmentService;

    /**
     * Get all enrollments (purchased courses) for the current authenticated user with pagination
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PaginationResponse> getMyEnrollments(
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        PaginationResponse response = enrollmentService.getMyEnrollments(pageable);
        return ResponseEntity.ok(response);
    }
}
