package com.english.api.blog.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BlogCategoryCreateRequest(
        @NotBlank @Size(max = 255) String name,
        // optional: if null, slug will be generated from name
        String slug,
        String description) {
}
