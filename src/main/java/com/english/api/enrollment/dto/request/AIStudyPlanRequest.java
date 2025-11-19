package com.english.api.enrollment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record AIStudyPlanRequest(
    @NotBlank(message = "Goal is required")
    String goal,

    @Positive(message = "Total time must be positive")
    Integer totalTime,

    String notes
) {}
