package com.english.api.course.dto.response;

import lombok.Builder;

/**
 * DTO for instructor statistics
 * Created for optimized statistics retrieval using PostgreSQL functions
 */
@Builder
public record InstructorStatsResponse(
    Long totalCourses,
    Long publishedCourses,
    Long totalStudents,
    Long totalRevenueCents,
    String formattedRevenue
) {
}
