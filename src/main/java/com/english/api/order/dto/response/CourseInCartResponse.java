package com.english.api.order.dto.response;

import java.util.UUID;

public record CourseInCartResponse(
    UUID id,
    String title,
    String slug,
    String description,
    String thumbnail,
    String language,
    Long priceCents,
    String currency,
    String instructorName,
    boolean isPurchased
) {}
