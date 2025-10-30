package com.english.api.user.dto.request;

import jakarta.validation.constraints.Positive;

/**
 * Request DTO for updating pending instructor request
 * Created by hungpham on 10/30/2025
 */
public record UpdateInstructorRequest(
        String bio,

        String expertise,

        @Positive(message = "Experience years must be positive")
        Integer experienceYears,

        String qualification,

        String reason
) {
}
