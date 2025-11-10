package com.english.api.enrollment.dto.response;

import com.english.api.enrollment.model.StudyPlanSchedule;

import java.time.OffsetDateTime;
import java.util.UUID;

public record StudyPlanScheduleResponse(
    UUID id,
    OffsetDateTime startTime,
    Integer durationMin,
    String taskDesc,
    StudyPlanSchedule.TaskStatus status,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {}
