package com.english.api.content.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record PostCreateRequest(
    @NotBlank
    @Size(max = 300)
    String title,

    // optional: if null, slug will be generated
    String slug,

    @NotBlank
    String bodyMd,

    // category ids to link
    List<UUID> categoryIds
) {}
