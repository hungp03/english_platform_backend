package com.english.api.forum.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record ForumThreadCreateRequest(
        @NotBlank(message = "Thread title is required")
        @Size(min = 5, max = 255, message = "Thread title must be between 5 and 255 characters")
        String title,

        @NotBlank(message = "Thread content is required")
        String bodyMd,

        @NotEmpty(message = "At least one category is required")
        @Size(max = 5, message = "Cannot assign more than 5 categories to a thread")
        List<UUID> categoryIds
) {
}
