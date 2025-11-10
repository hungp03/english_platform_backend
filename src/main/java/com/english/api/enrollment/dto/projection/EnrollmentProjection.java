package com.english.api.enrollment.dto.projection;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Projection interface for enrollment details query optimization
 * Created by hungpham on 11/05/2025
 */
public interface EnrollmentProjection {
    UUID getEnrollmentId();
    UUID getCourseId();
    String getCourseTitle();
    BigDecimal getProgressPercent();
}
