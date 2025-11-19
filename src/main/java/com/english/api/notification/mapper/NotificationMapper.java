package com.english.api.notification.mapper;

import com.english.api.notification.dto.response.NotificationResponse;
import com.english.api.notification.model.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {
    
    public NotificationResponse toResponse(Notification notification) {
        if (notification == null) {
            return null;
        }
        
        return new NotificationResponse(
                notification.getId(),
                notification.getUserId(),
                notification.getTitle(),
                notification.getContent(),
                notification.getIsRead(),
                notification.getCreatedAt(),
                notification.getReadAt()
        );
    }
}