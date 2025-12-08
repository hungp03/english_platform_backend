package com.english.api.enrollment.service;

import com.english.api.course.dto.response.LessonResponse;
import com.english.api.enrollment.dto.response.EnrollmentDetailResponse;
import com.english.api.enrollment.dto.response.EnrollmentResponse;
import com.english.api.order.model.Order;

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
     * Get all enrollments (purchased courses) for the current authenticated user
     */
    List<EnrollmentResponse> getMyEnrollments();

    /**
     * Get enrollment details for a specific course including course name, progress, and published modules
     * Verifies that the user has access to the course through enrollment
     */
    EnrollmentDetailResponse getEnrollmentDetails(String courseSlug);

    LessonResponse getLessonWithEnrollmentCheck(UUID lessonId);
}
