package com.english.api.content.dto.request;

import jakarta.validation.constraints.Size;

public record CategoryUpdateRequest(
    @Size(max = 255)
    String name,
    
    String slug,
    
    String description
) {}
