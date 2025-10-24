package com.english.api.forum.dto.request;

import java.util.UUID;

public record ForumPostCreateRequest(
    String bodyMd,
    UUID parentId
) {}
