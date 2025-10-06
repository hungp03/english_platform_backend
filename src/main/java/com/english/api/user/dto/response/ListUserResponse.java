package com.english.api.user.dto.response;

import java.time.Instant;
import java.util.UUID;

/**
 * Created by hungpham on 10/1/2025
 */
public record ListUserResponse(
        UUID id,
        String email,
        String fullName,
        String avatarUrl,
        boolean isActive,
        Instant createdAt
) {}
