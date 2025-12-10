package com.english.api.user.dto.request;

import jakarta.validation.constraints.Positive;

public record UpdateInstructorProfileRequest(
        String bio,
        String expertise,
        @Positive(message = "Experience years must be positive")
        Integer experienceYears,
        String qualification
) {
}
