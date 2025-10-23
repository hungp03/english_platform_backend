package com.english.api.user.dto.request;

import jakarta.validation.constraints.NotNull;

public record ReviewInstructorRequest(
    @NotNull(message = "Action is required")
    ApprovalAction action,

    String adminNotes
) {
    public enum ApprovalAction {
        APPROVE,
        REJECT
    }
}