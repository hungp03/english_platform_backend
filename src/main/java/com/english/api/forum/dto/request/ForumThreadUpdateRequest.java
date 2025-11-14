package com.english.api.forum.dto.request;

import java.util.List;
import java.util.UUID;

public record ForumThreadUpdateRequest(
    String title,
    String bodyMd,
    List<UUID> categoryIds,
    Boolean locked
) {}
