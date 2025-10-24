package com.english.api.order.dto.response;

import com.english.api.order.model.enums.CurrencyType;

import java.util.List;
import java.util.UUID;

/**
 * Response DTO for revenue analytics data
 * Requirements: 8.1, 8.2, 8.3 - Revenue statistics and analytics for instructors
 */
public record RevenueStatsResponse(
        UUID instructorId,
        RevenuePeriod period,
        Long totalRevenue,
        Integer totalOrders,
        Long averageOrderValue,
        CurrencyType currency,
        List<CourseRevenueBreakdown> courseBreakdown,
        List<MonthlyRevenue> monthlyRevenue
) {}