package com.english.api.user.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for reviewing instructor request
 * Created by hungpham on 10/29/2025
 */
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
