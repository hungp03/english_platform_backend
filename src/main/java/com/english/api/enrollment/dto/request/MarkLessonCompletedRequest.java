package com.english.api.enrollment.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record MarkLessonCompletedRequest(
    @NotNull(message = "Lesson ID is required")
    UUID lessonId,

    UUID enrollmentId
) {}
