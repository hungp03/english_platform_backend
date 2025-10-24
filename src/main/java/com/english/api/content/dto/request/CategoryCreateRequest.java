package com.english.api.content.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryCreateRequest(
    @NotBlank
    @Size(max = 255)
    String name,

    // optional: if null, slug will be generated from name
    String slug,

    String description
) {}
