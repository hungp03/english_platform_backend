package com.english.api.forum.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ForumThreadListResponse(
        UUID id,
        UUID authorId,
        String authorName,
        String authorAvatarUrl,
        String title,
        String slug,
        boolean locked,
        long viewCount,
        long replyCount,
        Instant lastPostAt,
        UUID lastPostId,
        UUID lastPostAuthor,
        Instant createdAt,
        Instant updatedAt,
        List<ForumCategoryResponse> categories
) {
}
