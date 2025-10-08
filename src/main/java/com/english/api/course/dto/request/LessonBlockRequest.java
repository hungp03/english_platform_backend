package com.english.api.course.dto.request;

import com.english.api.common.util.validator.ValidJson;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Created by hungpham on 10/4/2025
 */
public record LessonBlockRequest(
        @NotBlank(message = "Block type must not be blank")
        String blockType,

        @ValidJson
        @NotBlank(message = "Payload must not be blank")
        String payload,

//        @NotNull(message = "Position is required")
        @Min(value = 1, message = "Position must be greater than or equal to 1")
        Integer position,

        UUID mediaId // optional
) {}