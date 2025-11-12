package com.english.api.blog.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CommentUpdateRequest(
    @NotBlank
    String bodyMd
) {}
