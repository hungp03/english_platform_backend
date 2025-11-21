package com.english.api.enrollment.dto.request;

import java.math.BigDecimal;

public record EnrollmentProgressData(
    String courseTitle,
    BigDecimal progressPercent
) {}
