package com.english.api.user.dto.response;

import java.util.List;
import java.util.UUID;
import java.time.Instant;
public record AdminUserResponse(
        UUID id,
        String fullName,
        String email,
        boolean active,
        Instant createdAt,
        List<String> roles
) {
}
