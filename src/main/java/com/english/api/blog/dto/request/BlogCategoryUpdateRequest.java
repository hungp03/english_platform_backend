package com.english.api.blog.dto.request;

import jakarta.validation.constraints.Size;

public record BlogCategoryUpdateRequest(
        @Size(max = 255) String name,
        String slug,
        String description) {
}
