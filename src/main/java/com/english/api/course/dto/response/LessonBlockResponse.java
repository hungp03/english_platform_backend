package com.english.api.course.dto.response;

import java.util.UUID;

/**
 * Created by hungpham on 10/4/2025
 */
public record LessonBlockResponse(
        UUID id,
        String blockType,
        String payload,
        Integer position
) {}

