package com.english.api.course.dto.response;

import java.util.UUID;

/**
 * Created by hungpham on 10/4/2025
 */
public record TaggingResponse(
        UUID tagId,
        String entity,
        UUID entityId
) {}

