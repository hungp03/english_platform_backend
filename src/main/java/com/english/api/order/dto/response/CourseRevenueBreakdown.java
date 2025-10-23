package com.english.api.order.dto.response;

import java.util.UUID;

/**
 * DTO for course-specific revenue breakdown
 * Requirements: 8.1, 8.2, 8.3 - Revenue analytics with course-level breakdown
 */
public record CourseRevenueBreakdown(
        UUID courseId,
        String courseName,
        Long revenue,
        Integer orderCount,
        Long averagePrice
) {}