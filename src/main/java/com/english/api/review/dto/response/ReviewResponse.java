package com.english.api.review.dto.response;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for course review details
 */
public record ReviewResponse(
    UUID id,
    UUID courseId,
    String courseTitle,
    UUID userId,
    String userName,
    String userAvatarUrl,
    Integer rating,
    String comment,
    Boolean isPublished,
    Instant createdAt,
    Instant updatedAt
) {
}
