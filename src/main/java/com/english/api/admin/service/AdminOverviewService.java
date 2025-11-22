package com.english.api.admin.service;

import com.english.api.admin.dto.response.*;

public interface AdminOverviewService {

    // === 1. DASHBOARD SUMMARY ===
    DashboardSummaryResponse getDashboardSummary();

    // === 2. PENDING ACTIONS ===
    PendingActionsResponse getPendingActions();

    // === 3. CHARTS ===
    UserGrowthResponse getUserGrowthChart(int months);

    RevenueChartResponse getRevenueChart(int months);

    EnrollmentChartResponse getEnrollmentChart(int months);

    // === 4. TOP PERFORMERS ===
    TopPerformersResponse getTopCourses(int limit);

    TopPerformersResponse getTopInstructors(int limit);

    TopPerformersResponse getTopRevenueCourses(int limit);

    // === 5. EXPORT ===
    byte[] exportDashboardData(String type);

}
