package com.english.api.course.dto.response;

import java.time.Instant;
import java.util.UUID;

/**
 * Created by hungpham on 10/4/2025
 */
public record MediaAssetResponse(
        UUID id,
        String mimeType,
        String url,
        String meta,
        Instant createdAt
) {}

