package com.english.api.enrollment.dto.response;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for course module with nested lessons and progress
 * Created by hungpham on 11/08/2025
 */
public record CourseModuleWithLessonsResponse(
        UUID id,
        String title,
        Integer position,
        Boolean published,
        List<LessonWithProgressResponse> lessons
) implements Serializable {}
