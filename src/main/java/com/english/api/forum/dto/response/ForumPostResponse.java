package com.english.api.forum.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ForumPostResponse(
    UUID id,
    UUID threadId,
    UUID parentId,
    UUID authorId,
    String authorName,
    String authorAvatarUrl,
    String bodyMd,
    boolean published,
    Instant createdAt,
    Instant updatedAt
) {}
