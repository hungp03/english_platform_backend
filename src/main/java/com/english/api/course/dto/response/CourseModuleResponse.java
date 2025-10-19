package com.english.api.course.dto.response;

import java.util.UUID;

/**
 * Created by hungpham on 10/4/2025
 */
public record CourseModuleResponse(
        UUID id,
        String title,
        Integer position,
        Boolean published,
        Long lessonCount
) {}


