package com.english.api.forum.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ForumPostCreateRequest(
        @NotBlank(message = "Post content is required")
        @Size(min = 1, max = 10000, message = "Post content must be between 1 and 10000 characters")
        String bodyMd,

        UUID parentId
) {
}
