package com.english.api.blog.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CommentCreateRequest(
        UUID parentId,
        @NotBlank
        String bodyMd
) {
}
