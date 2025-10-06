package com.english.api.content.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryUpdateRequest {
    @Size(max = 255)
    private String name;
    private String slug;
    private String description;
}