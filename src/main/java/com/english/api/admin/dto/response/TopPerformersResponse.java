package com.english.api.admin.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record TopPerformersResponse(
    List<TopCourse> topCourses,
    List<TopInstructor> topInstructors,
    List<TopRevenueCourse> topRevenueCourses
) {
    public record TopCourse(
        UUID id,
        String title,
        String slug,
        String thumbnail,
        String instructorName,
        UUID instructorId,
        Long enrollmentCount,
        Long completionCount,
        BigDecimal completionRate,
        BigDecimal averageRating,
        Long totalRevenueCents,
        String currency,
        Integer rank
    ) {}
    
    public record TopInstructor(
        UUID id,
        String fullName,
        String email,
        String avatarUrl,
        Long totalCourses,
        Long publishedCourses,
        Long totalEnrollments,
        Long totalStudents,
        BigDecimal averageRating,
        Long totalRevenueCents,
        Integer rank
    ) {}
    
    public record TopRevenueCourse(
        UUID id,
        String title,
        String slug,
        String instructorName,
        Long totalRevenueCents,
        String currency,
        Long totalOrders,
        Long enrollmentCount,
        BigDecimal averageOrderValue,
        Integer rank
    ) {}
}
