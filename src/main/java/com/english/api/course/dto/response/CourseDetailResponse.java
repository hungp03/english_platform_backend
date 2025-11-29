package com.english.api.course.dto.response;

import com.english.api.course.model.enums.CourseStatus;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Created by hungpham on 10/18/2025
 */
public record CourseDetailResponse(
        UUID id,
        String title,
        String slug,
        String description,
        String detailedDescription,
        String language,
        String thumbnail,
        String[] skillFocus,
        Long priceCents,
        String currency,
        CourseStatus status,
        UUID instructorId,
        String createdBy,
        Instant updatedAt,
        Long moduleCount,
        Long lessonCount,
        Long studentCount,
        Double averageRating,
        Long totalReviews
) implements Serializable {
}
