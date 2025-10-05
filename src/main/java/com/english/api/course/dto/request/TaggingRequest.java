package com.english.api.course.dto.request;

/**
 * Created by hungpham on 10/4/2025
 */
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record TaggingRequest(
        @NotNull(message = "Tag ID is required")
        UUID tagId,

        @NotBlank(message = "Entity type must not be blank")
        String entity, // e.g., "COURSE", "LESSON"

        @NotNull(message = "Entity ID is required")
        UUID entityId
) {}

