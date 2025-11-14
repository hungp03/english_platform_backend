package com.english.api.notification.service.impl;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.english.api.auth.util.SecurityUtil;
import com.english.api.common.dto.PaginationResponse;
import com.english.api.notification.model.Notification;
import com.english.api.notification.model.UserFcmToken;
import com.english.api.notification.repository.NotificationRepository;
import com.english.api.notification.repository.UserFcmTokenRepository;
import com.english.api.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserFcmTokenRepository userFcmTokenRepository;

    @Async
    @Override
    public void saveNotification(UUID userId, String title, String content){
        Notification n = new Notification();
        n.setUserId(userId);
        n.setTitle(title);
        n.setContent(content);
        notificationRepository.save(n);
    }

    @Async
    @Override
    public void sendNotification(UUID userId, String title, String content) {
        saveNotification(userId, title, content);

        List<UserFcmToken> tokens = userFcmTokenRepository.findByUserId(userId);

        for (UserFcmToken t : tokens) {
            try {
                Message message = Message.builder()
                        .setToken(t.getToken())
                        .putData("title", title)
                        .putData("body", content)
                        .build();
                FirebaseMessaging.getInstance().send(message);
            } catch (FirebaseMessagingException e) {
                System.err.println("Failed to send FCM notification: " + e.getMessage());
            } catch (IllegalStateException e) {
                System.err.println("Firebase not initialized. Push notifications are disabled.");
                break;
            }
        }
    }

    @Transactional
    @Override
    public void registerToken(UUID userId, String token) {
        boolean exists = userFcmTokenRepository.existsByUserIdAndToken(userId, token);
        if (!exists) {
            UserFcmToken userToken = UserFcmToken.builder()
                    .userId(userId)
                    .token(token)
                    .build();
            userFcmTokenRepository.save(userToken);
        }
    }

    @Override
    public PaginationResponse getNotificationsByUser(int page, int size) {
        UUID userId = SecurityUtil.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return PaginationResponse.from(notifications, pageable);
    }

    @Async
    @Transactional
    @Override
    public void markAsRead(Long notificationId) {
        notificationRepository.markAsRead(notificationId, LocalDateTime.now());
    }

    @Async
    @Transactional
    @Override
    public void markAllAsRead(UUID userId) {
        notificationRepository.markAllAsRead(userId, LocalDateTime.now());
    }

    @Async
    @Transactional
    @Override
    public void deleteNotification(Long notificationId, UUID userId) {
        notificationRepository.findByIdAndUserId(notificationId, userId)
                .ifPresent(notificationRepository::delete);
    }

    @Async
    @Transactional
    @Override
    public void deleteAllNotifications(UUID userId) {
        notificationRepository.deleteByUserId(userId);
    }
}
