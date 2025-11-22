package com.english.api.blog.dto.response;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PublicPostDetailResponse(
        UUID id,
        String title,
        String slug,
        String bodyMd,
        Instant publishedAt,
        Instant createdAt,
        UUID authorId,
        String authorName,
        String authorAvatarUrl,
        List<BlogCategoryResponse> categories
) implements Serializable {
}
