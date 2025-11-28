package com.english.api.admin.dto.response;

public record PendingActionsResponse(
    Long instructorRequestsCount,
    Long forumReportsCount,
    Long pendingOrdersCount
) {
}
