package com.english.api.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request DTO for creating instructor request
 * Created by hungpham on 10/29/2025
 */
public record CreateInstructorRequest(
        @NotBlank(message = "Bio is required")
        String bio,

        @NotBlank(message = "Expertise is required")
        String expertise,

        @NotNull(message = "Experience years is required")
        @Positive(message = "Experience years must be positive")
        Integer experienceYears,

        @NotBlank(message = "Qualification is required")
        String qualification,

        @NotBlank(message = "Reason is required")
        String reason
) {
}
