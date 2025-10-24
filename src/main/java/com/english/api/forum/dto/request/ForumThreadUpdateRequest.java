package com.english.api.forum.dto.request;

import java.util.List;
import java.util.UUID;

public record ForumThreadUpdateRequest(
    String title,
    String bodyMd,
    java.util.List<java.util.UUID> categoryIds,
    Boolean locked
) {}
