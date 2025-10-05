package com.english.api.user.controller;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.user.dto.request.UpdatePasswordRequest;
import com.english.api.user.dto.request.UpdateUserRequest;
import com.english.api.user.dto.response.UserResponse;
import com.english.api.user.dto.response.UserUpdateResponse;
import com.english.api.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
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

    @PutMapping(value = "me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserUpdateResponse> updateCurrentUser(@Valid @ModelAttribute UpdateUserRequest request) throws IOException {
        return ResponseEntity.ok(userService.updateCurrentUser(request));
    }

    @PatchMapping("password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody UpdatePasswordRequest request){
        userService.changePassword(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<PaginationResponse> getUsers(
            Pageable pageable,
            @RequestParam(defaultValue = "", required = false) String searchTerm) {
        return ResponseEntity.ok(userService.getUsers(searchTerm, pageable));
    }

    @PatchMapping("/{userId}/toggle-status")
    public ResponseEntity<Void> toggleUserStatus(
            @PathVariable UUID userId) {
        userService.toggleUserStatus(userId);
        return ResponseEntity.ok().build();
    }
}
