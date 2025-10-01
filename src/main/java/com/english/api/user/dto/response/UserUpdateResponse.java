package com.english.api.user.dto.response;

import java.time.Instant;

/**
 * Created by hungpham on 10/1/2025
 */
public record UserUpdateResponse(
        String fullName,
        String email,
        String avatarUrl,
        Instant updatedAt
) {
}
