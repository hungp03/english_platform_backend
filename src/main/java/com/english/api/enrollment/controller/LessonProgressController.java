package com.english.api.enrollment.controller;

import com.english.api.enrollment.dto.request.MarkLessonCompletedRequest;
import com.english.api.enrollment.service.LessonProgressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for LessonProgress operations
 * Created by hungpham on 10/29/2025
 */
@RestController
@RequestMapping("/api/lesson-progress")
@RequiredArgsConstructor
public class LessonProgressController {
    private final LessonProgressService lessonProgressService;

    /**
     * Mark a lesson as completed for the current authenticated user
     * Automatically creates a LessonProgress record if it doesn't exist
     */
    @PostMapping("/mark-completed")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markCompleted(@Valid @RequestBody MarkLessonCompletedRequest request) {
        lessonProgressService.markCompleted(request.lessonId(), request.enrollmentId());
        return ResponseEntity.noContent().build();
    }
}
