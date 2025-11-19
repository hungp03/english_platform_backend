package com.english.api.user.dto.response;

import java.util.List;
import java.util.UUID;

/**
 * Created by hungpham on 9/24/2025
 */
public record UserResponse(
        UUID id,
        String email,
        String fullName,
        String avatarUrl,
        String provider,
        List<String>roles
) {
}
