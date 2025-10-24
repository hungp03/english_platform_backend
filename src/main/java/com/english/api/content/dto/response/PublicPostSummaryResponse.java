package com.english.api.content.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PublicPostSummaryResponse(
    UUID id,
    String title,
    String slug,
    boolean published,
    Instant publishedAt,
    Instant createdAt,
    List<CategoryResponse> categories
) {
}