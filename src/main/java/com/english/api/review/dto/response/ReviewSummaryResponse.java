package com.english.api.review.dto.response;

import java.time.Instant;
import java.util.UUID;

/**
 * Simplified review response for list/summary views
 */
public record ReviewSummaryResponse(
    UUID id,
    UUID userId,
    String userName,
    String userAvatarUrl,
    Integer rating,
    String comment,
    Instant createdAt
) {
}
