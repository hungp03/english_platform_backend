package com.english.api.course.dto.response;

import java.util.UUID;

/**
 * Created by hungpham on 10/17/2025
 */
public record LessonSummaryResponse(
        UUID id,
        String title,
        String kind,
        Integer estimatedMin,
        Integer position,
        Boolean isFree,
        Boolean published
) {}
