package com.english.api.cart.dto.response;

public record CartResponse(
    long totalPublishedCourses,
    Long totalPriceCents,
    String currency
) {}
