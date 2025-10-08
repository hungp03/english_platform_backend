package com.english.api.course.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Created by hungpham on 10/7/2025
 */
public record LessonAssetRequest(
        @NotNull(message = "Asset ID is required")
        UUID assetId
) {}

