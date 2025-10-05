package com.english.api.course.dto.request;

/**
 * Created by hungpham on 10/4/2025
 */
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TagRequest(
        @NotBlank(message = "Tag name must not be blank")
        @Size(max = 50, message = "Tag name must not exceed 50 characters")
        String name
) {}

