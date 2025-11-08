package com.english.api.course.dto.response;

import lombok.Builder;

import java.util.List;

/**
 * DTO for monthly growth statistics
 * Contains revenue and student count broken down by weekly periods
 */
@Builder
public record MonthlyGrowthResponse(
    Integer year,
    Integer month,
    Long totalRevenueCents,
    String formattedTotalRevenue,
    Long totalStudents,
    List<GrowthPeriodResponse> periods
) {
}
