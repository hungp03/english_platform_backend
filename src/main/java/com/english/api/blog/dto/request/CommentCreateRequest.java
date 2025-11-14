package com.english.api.blog.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;

public record CommentCreateRequest(
    UUID parentId, 
    @NotBlank
    String bodyMd
) {}
