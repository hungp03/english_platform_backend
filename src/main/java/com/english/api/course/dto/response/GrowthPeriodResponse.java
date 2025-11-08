package com.english.api.course.dto.response;

import lombok.Builder;

import java.time.LocalDate;

/**
 * DTO for a single growth period (weekly milestone within a month)
 */
@Builder
public record GrowthPeriodResponse(
    String periodLabel,
    LocalDate startDate,
    LocalDate endDate,
    Long revenueCents,
    String formattedRevenue,
    Long studentCount
) {
}
