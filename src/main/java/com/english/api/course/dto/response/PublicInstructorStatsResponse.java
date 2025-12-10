package com.english.api.course.dto.response;

import lombok.Builder;

@Builder
public record PublicInstructorStatsResponse(
    Long totalCourses,
    Long publishedCourses,
    Long totalStudents
) {
}