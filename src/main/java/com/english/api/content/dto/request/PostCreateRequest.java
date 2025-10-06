package com.english.api.content.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class PostCreateRequest {

    @NotBlank
    @Size(max = 300)
    private String title;

    // optional: if null, slug will be generated
    private String slug;

    @NotBlank
    private String bodyMd;

    // category ids to link
    private List<UUID> categoryIds;
}