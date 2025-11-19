package com.english.api.blog.dto.request;

import java.util.List;
import java.util.UUID;

public record PostUpdateRequest(
        String title,
        String bodyMd,
        List<UUID> categoryIds
) {
}
