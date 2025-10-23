package com.english.api.order.dto.response;

import java.time.LocalDate;

/**
 * DTO for revenue period information
 * Requirements: 8.1, 8.2, 8.3 - Revenue analytics with date range filtering
 */
public record RevenuePeriod(
        LocalDate from,
        LocalDate to
) {}