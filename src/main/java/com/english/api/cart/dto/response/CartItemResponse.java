package com.english.api.cart.dto.response;

import java.time.Instant;
import java.util.UUID;

public record CartItemResponse(
    UUID id,
    CourseInCartResponse course,
    Instant addedAt
) {}
