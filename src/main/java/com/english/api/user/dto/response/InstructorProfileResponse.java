package com.english.api.user.dto.response;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for InstructorProfile
 * Created by hungpham on 10/29/2025
 */
public record InstructorProfileResponse(
        UUID id,
        UUID userId,
        String fullName,
        String email,
        String avatarUrl,
        String bio,
        String expertise,
        Integer experienceYears,
        String qualification,
        Instant createdAt,
        Instant updatedAt
) {
}
