package com.english.api.user.dto.request;

import jakarta.validation.constraints.*;
import java.time.Instant;

public record CreateInstructorRequest(
    @NotBlank(message = "Bio is required")
    @Size(max = 2000, message = "Bio must be less than 2000 characters")
    String bio,

    @NotBlank(message = "Expertise is required")
    @Size(max = 1000, message = "Expertise must be less than 1000 characters")
    String expertise,

    @Min(value = 0, message = "Experience years cannot be negative")
    @Max(value = 50, message = "Experience years cannot exceed 50")
    Integer experienceYears,

    @Size(max = 2000, message = "Qualification must be less than 2000 characters")
    String qualification,

    @NotBlank(message = "Reason is required")
    @Size(max = 2000, message = "Reason must be less than 2000 characters")
    String reason
) {
}