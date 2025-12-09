package com.english.api.notification.controller;

import com.english.api.auth.util.SecurityUtil;
import com.english.api.common.dto.PaginationResponse;
import com.english.api.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    @PostMapping("/register-token")
    public ResponseEntity<String> registerToken(@RequestBody Map<String, String> body) {
        UUID userId = SecurityUtil.getCurrentUserId();
        String token = body.get("token");

        notificationService.registerToken(userId, token);
        return ResponseEntity.ok("Token saved");
    }

    @DeleteMapping("/remove-token")
    public ResponseEntity<String> removeToken(@RequestBody Map<String, String> body) {
        UUID userId = SecurityUtil.getCurrentUserId();
        String token = body.get("token");
        notificationService.removeToken(userId, token);
        return ResponseEntity.ok("Token removed");
    }

    @DeleteMapping("/remove-all-tokens")
    public ResponseEntity<String> removeAllTokens() {
        UUID userId = SecurityUtil.getCurrentUserId();
        notificationService.removeAllTokens(userId);
        return ResponseEntity.ok("All tokens removed");
    }

    @GetMapping
    public ResponseEntity<PaginationResponse> getUserNotifications(
            @PageableDefault(page = 0, size = 20) Pageable pageable) {
        return ResponseEntity.ok(notificationService.getNotificationsByUser(pageable));
    }

    @PostMapping("/mark-read/{id}")
    public ResponseEntity<String> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok("Notification marked as read");
    }

    @PostMapping("/mark-all-read")
    public ResponseEntity<String> markAllAsRead() {
        UUID userId = SecurityUtil.getCurrentUserId();
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok("All notifications marked as read");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteNotification(@PathVariable Long id) {
        UUID userId = SecurityUtil.getCurrentUserId();
        notificationService.deleteNotification(id, userId);
        return ResponseEntity.ok("Notification deleted");
    }

    @DeleteMapping
    public ResponseEntity<String> deleteAllNotifications() {
        UUID userId = SecurityUtil.getCurrentUserId();
        notificationService.deleteAllNotifications(userId);
        return ResponseEntity.ok("All notifications deleted");
    }
}
