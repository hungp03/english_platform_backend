package com.english.api.enrollment.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for detailed enrollment information with course and module data
 * Created by hungpham on 11/05/2025
 */
public record EnrollmentDetailResponse(
        UUID enrollmentId,
        UUID courseId,
        String courseName,
        BigDecimal progressPercent,
        List<CourseModuleWithLessonsResponse> publishedModules,
        UUID lastCompletedLessonId
) {}
