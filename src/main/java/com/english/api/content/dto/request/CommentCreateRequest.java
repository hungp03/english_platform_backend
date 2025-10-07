package com.english.api.content.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record CommentCreateRequest(
    UUID parentId,  // nullable
    @NotBlank
    String bodyMd
) {}
