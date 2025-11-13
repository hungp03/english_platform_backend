package com.english.api.enrollment.dto.response;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record StudyPlanDetailResponse(
    UUID id,
    String title,
    List<StudyPlanScheduleResponse> schedules,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {}
