package com.english.api.forum.dto.response;

import com.english.api.forum.entity.ReportTargetType;

import java.time.Instant;
import java.util.UUID;

public record ForumReportResponse(
        UUID id,
        ReportTargetType targetType,
        UUID targetId,
        UUID userId,
        String reporterName,
        String reporterEmail,
        String reason,
        String targetPreview,
        Boolean targetPublished,
        Instant createdAt,
        Instant resolvedAt,
        UUID resolvedBy
) {
}
