package com.english.api.forum.dto.request;

import com.english.api.forum.entity.ReportTargetType;
import java.util.UUID;

public record ForumReportCreateRequest(
    ReportTargetType targetType,
    UUID targetId,
    String reason
) {}
