package com.english.api.blog.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PostResponse(
        UUID id,
        UUID authorId,
        String title,
        String slug,
        String bodyMd,
        boolean published,
        Instant publishedAt,
        Instant createdAt,
        Instant updatedAt,
        List<BlogCategoryResponse> categories
) {
}
