package com.english.api.forum.dto.request;

public record ForumCategoryCreateRequest(
        String name,
        String slug,
        String description
) {
}
