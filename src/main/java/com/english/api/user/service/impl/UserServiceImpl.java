package com.english.api.user.service.impl;

import com.english.api.auth.util.SecurityUtil;
import com.english.api.common.exception.ResourceNotFoundException;
import com.english.api.user.dto.response.UserResponse;
import com.english.api.user.mapper.UserMapper;
import com.english.api.user.model.User;
import com.english.api.user.repository.UserRepository;
import com.english.api.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by hungpham on 9/23/2025
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

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
        User user = findById(userId);
        return userMapper.toUserResponse(user);
    }

    @Override
    @Cacheable(value = "userStatus", key = "#userId")
    public boolean isUserActive(UUID userId){
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

}
