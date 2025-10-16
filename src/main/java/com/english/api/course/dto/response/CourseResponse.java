package com.english.api.course.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Created by hungpham on 10/2/2025
 */
public record CourseResponse(
        UUID id,
        String title,
        String slug,
        String description,
        String language,
        String thumbnail,
        List<String> skillFocus,
        Long priceCents,
        String currency,
        boolean published,
        Instant publishedAt,
        Instant createdAt,
        Instant updatedAt
) {}
