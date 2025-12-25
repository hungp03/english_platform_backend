package com.english.api.forum.dto.response;

import java.time.Instant;
import java.util.UUID;

import com.english.api.forum.model.ReportTargetType;

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
        String reviewSlug,
        // UUID resolvedBy
        String resolvedBy
) {
}
