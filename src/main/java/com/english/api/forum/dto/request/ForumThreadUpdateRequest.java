package com.english.api.forum.dto.request;

import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record ForumThreadUpdateRequest(
        @Size(min = 5, max = 255, message = "Thread title must be between 5 and 255 characters")
        String title,

        @Size(min = 10, max = 50000, message = "Thread content must be between 10 and 50000 characters")
        String bodyMd,

        @Size(max = 5, message = "Cannot assign more than 5 categories to a thread")
        List<UUID> categoryIds,

        Boolean locked
) {
}
