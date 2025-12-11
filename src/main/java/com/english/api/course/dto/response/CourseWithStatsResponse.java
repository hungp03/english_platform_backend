package com.english.api.course.dto.response;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Created by hungpham on 10/14/2025
 */
public record CourseWithStatsResponse(
    UUID id,
    String title,
    String slug,
    String description,
    String language,
    String thumbnail,
    List<String> skillFocus,
    Long priceCents,
    String currency,
    String status,
    Long moduleCount,
    Long lessonCount,
    Long totalReviews,
    Double averageRating,
    Instant createdAt,
    Instant updatedAt
) implements Serializable {
}
