package com.english.api.enrollment.dto.response;

import java.util.UUID;

public record LessonWithProgressResponse(
        UUID id,
        UUID moduleId,
        String title,
        String kind,
        Integer estimatedMin,
        Integer position,
        Boolean isFree,
        Boolean published,
        Boolean isCompleted
) {}
