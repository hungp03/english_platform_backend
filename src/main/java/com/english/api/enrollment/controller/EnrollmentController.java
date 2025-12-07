package com.english.api.enrollment.controller;

import com.english.api.course.dto.response.LessonResponse;
import com.english.api.enrollment.dto.response.EnrollmentDetailResponse;
import com.english.api.enrollment.dto.response.EnrollmentResponse;
import com.english.api.enrollment.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
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
     * Get all enrollments (purchased courses) for the current authenticated user
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EnrollmentResponse>> getMyEnrollments() {
        List<EnrollmentResponse> response = enrollmentService.getMyEnrollments();
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
     * Get lesson details with enrollment verification
     * Returns lesson information if the user is enrolled in the course containing this lesson
     */
    @GetMapping("/lessons/{lessonId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LessonResponse> getLessonWithEnrollmentCheck(@PathVariable UUID lessonId) {
        LessonResponse lesson = enrollmentService.getLessonWithEnrollmentCheck(lessonId);
        return ResponseEntity.ok(lesson);
    }
}
