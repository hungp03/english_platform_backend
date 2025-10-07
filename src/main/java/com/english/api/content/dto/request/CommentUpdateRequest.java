package com.english.api.content.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CommentUpdateRequest(
    @NotBlank
    String bodyMd
) {}
