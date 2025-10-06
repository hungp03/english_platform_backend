package com.english.api.content.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryCreateRequest {
    @NotBlank
    @Size(max = 255)
    private String name;

    // optional: if null, slug will be generated from name
    private String slug;

    private String description;
}