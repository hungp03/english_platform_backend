package com.english.api.user.service;

import com.english.api.user.dto.response.UserResponse;
import com.english.api.user.model.User;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by hungpham on 9/23/2025
 */
public interface UserService {
    boolean existsByEmail(String email);
    Optional<User> findOptionalByEmail(String email);
    User save(User user);
    User findByEmail (String email);
    User findById(UUID uuid);
    UserResponse getCurrentUser();
    boolean isUserActive(UUID userId);
    void resetPassword(String email, String newPassword);
}
