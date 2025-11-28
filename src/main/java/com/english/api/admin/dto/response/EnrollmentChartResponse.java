package com.english.api.admin.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record EnrollmentChartResponse(
    List<MonthlyEnrollment> monthlyEnrollment
) {
    public record MonthlyEnrollment(
        String month,
        Long newEnrollments,
        Long completedEnrollments,
        BigDecimal completionRate,
        BigDecimal averageProgress
    ) {}
}
