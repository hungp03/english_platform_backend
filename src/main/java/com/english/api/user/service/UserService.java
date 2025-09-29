package com.english.api.user.service;

import com.english.api.user.dto.response.UserResponse;
import com.english.api.user.model.User;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.user.dto.request.UpdatePasswordRequest;
import com.english.api.user.dto.request.UpdateUserRequest;
import com.english.api.user.dto.response.AdminUserResponse;
import com.english.api.user.dto.response.UserUpdateResponse;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by hungpham on 9/23/2025
 */
public interface UserService {
    boolean existsByEmail(String email);
    Optional<User> findOptionalByEmail(String email);

    Optional<User> findOptionalByEmailWithRoles(String email);

    User save(User user);
    User findByEmail (String email);
    User findById(UUID uuid);
    UserResponse getCurrentUser();
    boolean isUserActive(UUID userId);
    void resetPassword(String email, String newPassword);
    UserUpdateResponse updateCurrentUser(UpdateUserRequest request);
    void updatePassword(UpdatePasswordRequest request);
    PaginationResponse getUsers(String searchTerm, int page, int size);
    void toggleUserStatus(UUID userId, String lockReason);
}
