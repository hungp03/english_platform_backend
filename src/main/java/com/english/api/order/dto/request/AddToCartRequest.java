package com.english.api.order.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddToCartRequest(
    @NotNull(message = "Course ID is required")
    UUID courseId
) {}
