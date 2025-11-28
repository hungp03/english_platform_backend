package com.english.api.admin.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record RevenueChartResponse(
    List<MonthlyRevenue> monthlyRevenue
) {
    public record MonthlyRevenue(
        String month,
        Long revenueVND,
        Long revenueUSD,
        Long totalOrders,
        BigDecimal averageOrderValue
    ) {}
}
