package com.english.api.user.dto.response;

import java.time.Instant;
import java.util.UUID;

public record InstructorRequestResponse(
    UUID id,
    UserResponse user,
    String status,
    String bio,
    String expertise,
    Integer experienceYears,
    String qualification,
    String reason,
    String adminNotes,
    Instant requestedAt,
    Instant reviewedAt,
    Instant createdAt,
    UserResponse reviewedBy
) {
}