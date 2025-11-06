package com.english.api.enrollment.service;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.enrollment.dto.response.EnrollmentDetailResponse;
import com.english.api.enrollment.dto.response.LessonWithProgressResponse;
import com.english.api.order.model.Order;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

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

    /**
     * Get enrollment details for a specific course including course name, progress, and published modules
     * Verifies that the user has access to the course through enrollment
     * @param courseSlug The slug of the course to get enrollment details for
     * @return EnrollmentDetailResponse containing course details, progress, and published modules
     * @throws org.springframework.security.access.AccessDeniedException if user is not enrolled in the course
     */
    EnrollmentDetailResponse getEnrollmentDetails(String courseSlug);

    /**
     * Get published lessons with completion status for a specific module
     * @param moduleId The ID of the module
     * @return List of LessonWithProgressResponse containing lesson data and completion status
     */
    List<LessonWithProgressResponse> getPublishedLessonsWithProgress(UUID moduleId);
}
