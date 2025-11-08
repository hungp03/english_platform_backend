package com.english.api.enrollment.service;

import java.util.UUID;

/**
 * Service interface for LessonProgress operations
 * Created by hungpham on 10/29/2025
 */
public interface LessonProgressService {
    /**
     * Mark a lesson as completed for the current authenticated user
     * Creates a new LessonProgress if it doesn't exist
     *
     * @param lessonId the lesson ID
     * @param enrollmentId the enrollment ID (optional)
     */
    void markCompleted(UUID lessonId, UUID enrollmentId);

    /**
     * Update enrollment progress percentage based on completed lessons
     * Calculates: (completed lessons / total published lessons) * 100
     *
     * @param userId the user ID
     * @param courseId the course ID
     */
    void updateEnrollmentProgress(UUID userId, UUID courseId);
}
