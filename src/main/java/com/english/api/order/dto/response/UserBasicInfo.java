package com.english.api.order.dto.response;

import java.util.UUID;

/**
 * Basic user information for order responses
 */
public record UserBasicInfo(
        UUID id,
        String fullName,
        String email
) {}