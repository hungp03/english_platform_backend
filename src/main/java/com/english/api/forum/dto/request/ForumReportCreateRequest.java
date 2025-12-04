package com.english.api.forum.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

import com.english.api.forum.model.ReportTargetType;

public record ForumReportCreateRequest(
        // @NotNull(message = "Target type is required")
        ReportTargetType targetType,

        // @NotNull(message = "Target ID is required")
        UUID targetId,

        @NotBlank(message = "Report reason is required")
        @Size(min = 10, max = 1000, message = "Report reason must be between 10 and 1000 characters")
        String reason
) {
}
