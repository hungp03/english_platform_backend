package com.english.api.content.dto.response;

import java.time.Instant;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        UUID postId,
        UUID parentId,
        UUID authorId,
        String bodyMd,
        boolean published,
        Instant createdAt,
        Instant updatedAt
) {}
