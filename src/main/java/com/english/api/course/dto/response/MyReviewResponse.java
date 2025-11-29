package com.english.api.course.dto.response;

import java.time.Instant;
import java.util.UUID;

public record MyReviewResponse(
    UUID id,
    UUID courseId,
    String courseTitle,
    String courseSlug,     
    String courseThumbnail,
    UUID userId,
    String userName,
    String userAvatarUrl,
    Integer rating,
    String comment,
    Boolean isPublished,
    Instant createdAt,
    Instant updatedAt
) {}