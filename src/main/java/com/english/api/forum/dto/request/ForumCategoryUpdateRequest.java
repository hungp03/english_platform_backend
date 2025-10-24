package com.english.api.forum.dto.request;

public record ForumCategoryUpdateRequest(
    String name,
    String slug,
    String description
) {}
