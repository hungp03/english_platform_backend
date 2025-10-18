package com.english.api.course.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Created by hungpham on 10/4/2025
 */
public record MediaAssetRequest(
        @NotBlank(message = "MIME type must not be blank")
        String mimeType,

        @NotBlank(message = "URL must not be blank")
        @Size(max = 2048, message = "URL must not exceed 2048 characters")
        String url,

        String meta // JSON string (optional)
) {}