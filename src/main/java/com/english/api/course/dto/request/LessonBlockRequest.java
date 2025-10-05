package com.english.api.course.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Created by hungpham on 10/4/2025
 */
public record LessonBlockRequest(
        @NotBlank(message = "Block type must not be blank")
        String blockType, // TEXT, MEDIA, QUIZ...

        @NotBlank(message = "Payload must not be blank")
        String payload, // JSON string

        @NotNull(message = "Position is required")
        @Min(value = 1, message = "Position must be greater than or equal to 1")
        Integer position
) {}