package com.english.api.notification.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
        Long id,
        UUID userId,
        String title,
        String content,
        Boolean isRead,
        LocalDateTime createdAt,
        LocalDateTime readAt
) {
}