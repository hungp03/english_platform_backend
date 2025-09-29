package com.english.api.user.controller;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.user.dto.request.ChangePasswordRequest;
import com.english.api.user.dto.request.UpdateUserRequest;
import com.english.api.user.dto.response.UserResponse;
import com.english.api.user.dto.response.UserUpdateResponse;
import com.english.api.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;

/**
 * Created by hungpham on 9/24/2025
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    @GetMapping("/exists")
    public ResponseEntity<Boolean> existsByEmail(@RequestParam String email) {
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestParam String email, @RequestParam String newPassword) {
        userService.resetPassword(email, newPassword);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/me")
    public ResponseEntity<UserUpdateResponse> updateCurrentUser(@RequestBody UpdateUserRequest request) {
        UserUpdateResponse response = userService.updateCurrentUser(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> updatePassword(@RequestBody ChangePasswordRequest request) {
        userService.updatePassword(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<PaginationResponse> getUsers(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PaginationResponse response = userService.getUsers(searchTerm, page, size);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{userId}/toggle-status")
    public ResponseEntity<Void> toggleUserStatus(@PathVariable UUID userId,
                                                 @RequestParam(required = false) String lockReason) {
        userService.toggleUserStatus(userId, lockReason);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}/active")
    public ResponseEntity<Boolean> isUserActive(@PathVariable UUID userId) {
        boolean active = userService.isUserActive(userId);
        return ResponseEntity.ok(active);
    }
}
