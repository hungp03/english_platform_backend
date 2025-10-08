package com.english.api.course.dto.request;

/**
 * Created by hungpham on 10/4/2025
 */
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CourseModuleRequest(
        @NotBlank(message = "Module title must not be blank")
        String title,

//        @NotNull(message = "Position is required")
        @Min(value = 1, message = "Position must be greater than or equal to 1")
        Integer position
) {}

