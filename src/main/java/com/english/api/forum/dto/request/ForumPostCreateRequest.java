package com.english.api.forum.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record ForumPostCreateRequest(
        @NotBlank(message = "Post content is required")
        String bodyMd,

        UUID parentId
) {
}
