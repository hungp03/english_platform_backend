package com.english.api.admin.dto.response;
import lombok.*;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueChartResponse {
    private List<MonthlyRevenue> monthlyRevenue;
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyRevenue {
        private String month;
        private Long revenueVND;
        private Long revenueUSD;
        private Long totalOrders;
        private BigDecimal averageOrderValue;
    }
}
