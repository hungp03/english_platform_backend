package com.english.api.enrollment.controller;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.enrollment.dto.response.EnrollmentDetailResponse;
import com.english.api.enrollment.dto.response.LessonWithProgressResponse;
import com.english.api.enrollment.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

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

    /**
     * Get enrollment details for a specific course including course name, progress, and published modules
     * Only accessible if the user is enrolled in the course
     */
    @GetMapping("/courses/{courseSlug}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EnrollmentDetailResponse> getEnrollmentDetails(@PathVariable String courseSlug) {
        EnrollmentDetailResponse response = enrollmentService.getEnrollmentDetails(courseSlug);
        return ResponseEntity.ok(response);
    }

    /**
     * Get published lessons with completion status for a specific module
     * Shows which lessons the current user has completed
     */
    @GetMapping("/modules/{moduleId}/lessons")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LessonWithProgressResponse>> getPublishedLessonsWithProgress(@PathVariable UUID moduleId) {
        List<LessonWithProgressResponse> lessons = enrollmentService.getPublishedLessonsWithProgress(moduleId);
        return ResponseEntity.ok(lessons);
    }
}
