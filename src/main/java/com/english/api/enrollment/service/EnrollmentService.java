package com.english.api.enrollment.service;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.order.model.Order;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for Enrollment operations
 * Created by hungpham on 10/29/2025
 */
public interface EnrollmentService {
    
    /**
     * Creates enrollments for all courses in a paid order
     */
    void createEnrollmentsAfterPayment(Order order);
    
    /**
     * Get all enrollments (purchased courses) for the current authenticated user with pagination
     * @param pageable Pagination information
     * @return PaginationResponse containing enrollment data and pagination metadata
     */
    PaginationResponse getMyEnrollments(Pageable pageable);
}
