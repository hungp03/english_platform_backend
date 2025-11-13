package com.english.api.blog.dto.response;

import java.time.Instant;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        UUID postId,
        UUID parentId,
        UUID authorId,
        String authorName,
        String authorAvatarUrl,
        String postTitle,
        String postSlug,
        String bodyMd,
        boolean published,
        Instant createdAt,
        Instant updatedAt
) {}
