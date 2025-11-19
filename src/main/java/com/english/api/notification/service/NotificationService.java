package com.english.api.notification.service;

import com.english.api.common.dto.PaginationResponse;

import java.util.UUID;

public interface NotificationService {
    void saveNotification(UUID userId, String title, String content);
    
    void sendNotification(UUID userId, String title, String content);
    
    void registerToken(UUID userId, String token);
    
    PaginationResponse getNotificationsByUser(int page, int size);
    
    void markAsRead(Long notificationId);
    
    void markAllAsRead(UUID userId);
    
    void deleteNotification(Long notificationId, UUID userId);
    
    void deleteAllNotifications(UUID userId);
}
