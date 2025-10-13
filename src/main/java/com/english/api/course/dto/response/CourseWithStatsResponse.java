package com.english.api.course.dto.response;

import java.time.Instant;
import java.util.UUID;

/**
 * Created by hungpham on 10/14/2025
 */
public record CourseWithStatsResponse (
        UUID id,
        String title,
        String description,
        String language,
        String[] skillFocus,
        Long priceCents,
        String currency,
        boolean published,
        Long moduleCount,
        Long lessonCount,
        Instant createdAt,
        Instant updatedAt
){}
