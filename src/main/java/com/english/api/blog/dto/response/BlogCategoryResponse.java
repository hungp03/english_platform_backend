package com.english.api.blog.dto.response;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public record BlogCategoryResponse(
        UUID id,
        String name,
        String slug,
        String description,
        Instant createdAt) implements Serializable {
}
