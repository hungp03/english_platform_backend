package com.english.api.user.dto.response;

import java.util.UUID;

public record UserUpdateResponse(
        UUID id,
        String fullName,
        String email,
        String phone
) {
}
