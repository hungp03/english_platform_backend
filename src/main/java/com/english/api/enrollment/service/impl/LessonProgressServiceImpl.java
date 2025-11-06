package com.english.api.enrollment.service.impl;

import com.english.api.auth.util.SecurityUtil;
import com.english.api.common.exception.AccessDeniedException;
import com.english.api.common.exception.ResourceNotFoundException;
import com.english.api.course.model.Lesson;
import com.english.api.course.repository.LessonRepository;
import com.english.api.enrollment.model.Enrollment;
import com.english.api.enrollment.model.LessonProgress;
import com.english.api.enrollment.repository.EnrollmentRepository;
import com.english.api.enrollment.repository.LessonProgressRepository;
import com.english.api.enrollment.service.LessonProgressService;
import com.english.api.user.model.User;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

/**
 * Implementation of LessonProgressService
 * Created by hungpham on 10/29/2025
 */
@Service
public class LessonProgressServiceImpl implements LessonProgressService {
    private final LessonProgressRepository lessonProgressRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonRepository lessonRepository;
    private final LessonProgressService self;

    public LessonProgressServiceImpl(
            LessonProgressRepository lessonProgressRepository,
            EnrollmentRepository enrollmentRepository,
            LessonRepository lessonRepository,
            @Lazy LessonProgressService self) {
        this.lessonProgressRepository = lessonProgressRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.lessonRepository = lessonRepository;
        this.self = self;
    }

    @Override
    @Transactional
    public void markCompleted(UUID lessonId, UUID enrollmentId) {
        UUID userId = SecurityUtil.getCurrentUserId();

        // Pre-check: Get courseId from lesson
        UUID courseId = lessonRepository.findCourseIdByLessonId(lessonId)
            .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));

        // Pre-check: Verify user is enrolled in the course
        if (!enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)) {
            throw new AccessDeniedException("You must be enrolled in this course to mark lessons as completed");
        }

        // Try to update existing record first (avoids SELECT query)
        int updatedRows = lessonProgressRepository.markAsCompleted(userId, lessonId);

        if (updatedRows == 0) {
            // No rows updated, create a new record
            LessonProgress newProgress = LessonProgress.builder()
                .user(User.builder().id(userId).build())
                .lesson(Lesson.builder().id(lessonId).build())
                .enrollment(enrollmentId != null ? Enrollment.builder().id(enrollmentId).build() : null)
                .completed(true)
                .build();

            lessonProgressRepository.save(newProgress);
        }

        // Update enrollment progress percentage asynchronously
        self.updateEnrollmentProgress(courseId);
    }

    @Override
    @Async
    @Transactional
    public void updateEnrollmentProgress(UUID courseId) {
        UUID userId = SecurityUtil.getCurrentUserId();

        // Get total published lessons in course
        long totalLessons = lessonRepository.countPublishedLessonsByCourseId(courseId);

        // Calculate progress percentage
        BigDecimal progressPercent;
        if (totalLessons == 0) {
            progressPercent = BigDecimal.ZERO;
        } else {
            // Get completed lessons count
            long completedLessons = lessonProgressRepository.countCompletedLessonsByUserAndCourse(userId, courseId);

            // Calculate percentage: (completed / total) * 100
            progressPercent = BigDecimal.valueOf(completedLessons)
                .divide(BigDecimal.valueOf(totalLessons), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
        }

        // Update enrollment
        enrollmentRepository.updateProgressPercent(userId, courseId, progressPercent);
    }
}
