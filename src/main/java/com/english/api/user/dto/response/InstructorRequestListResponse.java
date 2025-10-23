package com.english.api.user.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record InstructorRequestListResponse(
    List<InstructorRequestItem> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last,
    boolean empty
) {
    public record InstructorRequestItem(
        UUID id,
        UserSimpleResponse user,
        String status,
        String bio,
        String expertise,
        Integer experienceYears,
        Instant requestedAt,
        Instant reviewedAt
    ) {
    }

    public record UserSimpleResponse(
        UUID id,
        String fullName,
        String email
    ) {
    }
}