package com.english.api.user.dto.response;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for basic instructor information (without bio, expertise, qualification)
 * Created by hungpham on 11/09/2025
 */
public record InstructorBasicInfoResponse(
        UUID id,
        UUID userId,
        String fullName,
        String email,
        String avatarUrl,
        Integer experienceYears,
        boolean isActive,
        Instant createdAt
) {
}
