package com.english.api.course.dto.request;

import com.english.api.course.validator.ValidJsonContent;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Created by hungpham on 10/4/2025
 */
public record LessonRequest(
        @NotBlank(message = "Lesson title must not be blank")
        @Size(max = 255, message = "Lesson title must not exceed 255 characters")
        String title,

        @NotBlank(message = "Lesson kind must not be blank")
        String kind, // VIDEO, QUIZ, TEXT...

        @NotNull(message = "Estimated duration is required")
        @Min(value = 1, message = "Estimated duration must be at least 1 minute")
        Integer estimatedMin,

        @Min(value = 1, message = "Position must be greater than or equal to 1")
        Integer position,

        @NotNull(message = "isFree flag is required")
        Boolean isFree,

        @NotNull(message = "Content must not be null")
        @ValidJsonContent
        JsonNode content,

        UUID mediaId
) {}
