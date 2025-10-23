package com.english.api.order.dto.response;

/**
 * DTO for monthly revenue data
 * Requirements: 8.1, 8.2, 8.3 - Revenue analytics with time period breakdown
 */
public record MonthlyRevenue(
        String month, // Format: YYYY-MM
        Long revenue,
        Integer orderCount
) {}