package com.english.api.forum.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ForumCategoryResponse(
        UUID id,
        String name,
        String slug,
        String description,
        Instant createdAt
) {
}
