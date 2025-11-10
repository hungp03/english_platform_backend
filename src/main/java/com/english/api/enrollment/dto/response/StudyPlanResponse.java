package com.english.api.enrollment.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record StudyPlanResponse(
    UUID id,
    String title,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {}
