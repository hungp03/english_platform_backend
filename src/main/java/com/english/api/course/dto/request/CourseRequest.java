package com.english.api.course.dto.request;

import jakarta.validation.constraints.*;

import java.util.List;

/**
 * Created by hungpham on 10/2/2025
 */
public record CourseRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title,

        @Size(max = 2000, message = "Description must not exceed 2000 characters")
        String description,

        @NotBlank(message = "Language is required")
        @Size(max = 10, message = "Language code too long")
        String language,

        List<@NotBlank String> skillFocus,

        @NotNull(message = "Price is required")
        @Min(value = 0, message = "Price must be >= 0")
        Long priceCents,

        @NotBlank(message = "Currency is required")
        @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be 3 uppercase letters (e.g., USD, VND)")
        String currency
) {}
