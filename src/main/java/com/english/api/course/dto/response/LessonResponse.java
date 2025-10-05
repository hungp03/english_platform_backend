package com.english.api.course.dto.response;

import java.util.UUID;

/**
 * Created by hungpham on 10/4/2025
 */
public record LessonResponse(
        UUID id,
        String title,
        String kind,
        Integer estimatedMin,
        Integer position,
        Boolean isFree
) {}

