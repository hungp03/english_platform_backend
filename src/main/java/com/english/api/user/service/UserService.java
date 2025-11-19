package com.english.api.user.service;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.user.dto.request.UpdatePasswordRequest;
import com.english.api.user.dto.request.UpdateUserRequest;
import com.english.api.user.dto.response.UserResponse;
import com.english.api.user.dto.response.UserUpdateResponse;
import com.english.api.user.model.User;
import org.springframework.data.domain.Pageable;
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
    void changePassword(UpdatePasswordRequest request);
    UserUpdateResponse updateCurrentUser(UpdateUserRequest request);
    void toggleUserStatus(UUID userId);
    PaginationResponse getUsers(String searchTerm, Pageable pageable);
    boolean isUserActive(UUID userId);
    void resetPassword(String email, String newPassword);
    Optional<User> findByProviderAndProviderUid(String provider, String providerUid);
}
