package com.english.api.order.dto.response;

import java.util.UUID;

/**
 * DTO for course-specific revenue breakdown
 */
public record CourseRevenueBreakdown(
        UUID courseId,
        String courseName,
        Long revenue,
        Integer orderCount,
        Long averagePrice
) {}