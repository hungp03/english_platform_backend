package com.english.api.blog.dto.response;

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
    List<BlogCategoryResponse> categories
) {
}
