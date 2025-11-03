package com.english.api.enrollment.dto.response;

import com.english.api.enrollment.model.enums.EnrollmentStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Response DTO for enrollment information
 * Created by hungpham on 11/03/2025
 */
public record EnrollmentResponse(
        UUID id,
        UUID courseId,
        String courseTitle,
        String courseSlug,
        String courseThumbnail,
        EnrollmentStatus status,
        BigDecimal progressPercent,
        OffsetDateTime startedAt,
        OffsetDateTime completedAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
