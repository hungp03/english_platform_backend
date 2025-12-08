package com.english.api.admin.dto.response;

import java.util.List;

public record UserGrowthResponse(
    List<MonthlyData> monthlyData
) {
    public record MonthlyData(
        String month,
        Long newUsers,
        // Long activeUsers
        Long totalUsers
    ) {}
}
