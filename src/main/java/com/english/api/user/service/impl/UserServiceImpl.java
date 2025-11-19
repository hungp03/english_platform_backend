package com.english.api.user.service.impl;

import com.english.api.auth.util.SecurityUtil;
import com.english.api.common.dto.MediaUploadResponse;
import com.english.api.common.dto.PaginationResponse;
import com.english.api.common.exception.*;
import com.english.api.common.service.MediaService;
import com.english.api.user.dto.request.UpdatePasswordRequest;
import com.english.api.user.dto.request.UpdateUserRequest;
import com.english.api.user.dto.response.UserResponse;
import com.english.api.user.dto.response.UserUpdateResponse;
import com.english.api.user.mapper.UserMapper;
import com.english.api.user.model.User;
import com.english.api.user.repository.UserRepository;
import com.english.api.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by hungpham on 9/23/2025
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final MediaService mediaService;

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public Optional<User> findOptionalByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    public User findById(UUID uuid) {
        return userRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Override
    public UserResponse getCurrentUser() {
        UUID userId = SecurityUtil.getCurrentUserId();
        List<Object[]> rows = userRepository.findUserWithRoles(userId);

        if (rows.isEmpty()) {
            throw new ResourceNotFoundException("User not found: " + userId);
        }

        Object[] first = rows.getFirst();

        UUID id = (UUID) first[0];
        String email = (String) first[1];
        String fullName = (String) first[2];
        String avatarUrl = (String) first[3];
        String provider = (String) first[4];

        List<String> roles = rows.stream()
                .map(r -> (String) r[5])
                .filter(Objects::nonNull)
                .toList();

        return new UserResponse(id, email, fullName, avatarUrl, provider, roles);
    }

    @Transactional
    @Override
    public void changePassword(UpdatePasswordRequest request) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new ResourceInvalidException("Confirm password does not match.");
        }
        UUID uid = SecurityUtil.getCurrentUserId();
        User user = findById(uid);
        if (user.getPasswordHash() == null || user.getPasswordHash().isEmpty()) {
            throw new ResourceInvalidException("You account did not have password. Please login with Google or reset password.");
        }
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Old password does not match.");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    @Transactional
    @Override
    public UserUpdateResponse updateCurrentUser(UpdateUserRequest request) {
        UUID userId = SecurityUtil.getCurrentUserId();
        User user = findById(userId);

        // === Update email ===
        if (request.email() != null && !request.email().isBlank()) {
            if (!user.getEmail().equals(request.email()) && existsByEmail(request.email())) {
                throw new ResourceAlreadyExistsException("Email is already in use");
            }
            user.setEmail(request.email());
        }

        // === Update full name ===
        if (request.fullName() != null && !request.fullName().isBlank()) {
            user.setFullName(request.fullName());
        }

        // === Handle avatar upload ===
        MultipartFile avatarFile = request.avatarFile();
        if (avatarFile != null && !avatarFile.isEmpty()) {
            try {
                // Xóa ảnh cũ nếu có
                if (user.getAvatarUrl() != null && !user.getAvatarUrl().isBlank()) {
                    mediaService.deleteFileByUrl(user.getAvatarUrl());
                }

                // Upload ảnh mới
                MediaUploadResponse uploaded = mediaService.uploadFile(avatarFile, "users");
                user.setAvatarUrl(uploaded.url());

            } catch (IOException e) {
                log.error("Failed to upload or delete avatar: {}", e.getMessage());
            } catch (Exception e) {
                log.error("Unexpected error when handling avatar: {}", e.getMessage());
            }
        }

        // JPA auto update on commit
        return userMapper.toUpdateResponse(user);
    }

    @Transactional
    @CacheEvict(value = "userStatus", key = "#userId")
    @Override
    public void toggleUserStatus(UUID userId) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        if (currentUserId.equals(userId)) {
            throw new OperationNotAllowedException("You cannot change your own status");
        }

        User user = findById(userId);
        user.setActive(!user.isActive());
        userRepository.save(user);
    }

    @Override
    public PaginationResponse getUsers(String searchTerm, Pageable pageable) {
        Page<User> pageResult = (searchTerm == null || searchTerm.isBlank())
                ? userRepository.findAll(pageable)
                : userRepository.findByFullNameOrEmail(searchTerm, pageable);

        return PaginationResponse.from(pageResult.map(userMapper::toListUserResponse), pageable);
    }

    @Override
    @Cacheable(value = "userStatus", key = "#userId")
    public boolean isUserActive(UUID userId) {
        Boolean active = userRepository.isUserActive(userId);
        if (active == null) {
            throw new ResourceNotFoundException("Invalid session: user not found. Please sign in again.");
        }
        return active;
    }

    @Override
    public void resetPassword(String email, String newPassword) {
        User user = findByEmail(email);
        user.setPasswordHash(newPassword);
        userRepository.save(user);
    }

    @Override
    public Optional<User> findByProviderAndProviderUid(String provider, String providerUid) {
        return userRepository.findByProviderAndProviderUidWithRoles(provider, providerUid);
    }

}
