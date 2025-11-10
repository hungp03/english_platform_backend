package com.english.api.enrollment.dto.request;

import com.english.api.enrollment.model.StudyPlanSchedule;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.UUID;

public record StudyPlanScheduleRequest(
    UUID id,

    @NotNull(message = "Start time is required")
    OffsetDateTime startTime,

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    Integer durationMin,

    @NotNull(message = "Task description is required")
    String taskDesc,

    StudyPlanSchedule.TaskStatus status
) {}
