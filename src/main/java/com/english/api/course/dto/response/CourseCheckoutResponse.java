package com.english.api.course.dto.response;

import java.io.Serializable;
import java.util.UUID;

/**
 * DTO for course information needed for checkout payment display.
 * Contains only essential fields to minimize data transfer.
 */
public record CourseCheckoutResponse(
    UUID id,
    String title,
    String thumbnail,
    Long priceCents,
    String currency
) implements Serializable {
}