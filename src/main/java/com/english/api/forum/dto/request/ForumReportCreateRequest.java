package com.english.api.forum.dto.request;

import java.util.UUID;

import com.english.api.forum.model.ReportTargetType;

public record ForumReportCreateRequest(
        ReportTargetType targetType,
        UUID targetId,
        String reason
) {
}
