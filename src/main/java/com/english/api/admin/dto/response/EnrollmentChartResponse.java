package com.english.api.admin.dto.response;

import java.math.BigDecimal;
import java.util.List;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentChartResponse {
    private List<MonthlyEnrollment> monthlyEnrollment;
    
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyEnrollment {
        private String month;
        private Long newEnrollments;
        private Long completedEnrollments;
        private BigDecimal completionRate;
        private BigDecimal averageProgress;
    }
}
