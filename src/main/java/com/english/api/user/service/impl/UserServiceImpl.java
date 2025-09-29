package com.english.api.user.service.impl;

import com.english.api.auth.util.SecurityUtil;
import com.english.api.common.exception.ResourceNotFoundException;
import com.english.api.user.dto.response.UserResponse;
import com.english.api.user.mapper.UserMapper;
import com.english.api.user.model.User;
import com.english.api.user.repository.UserRepository;
import com.english.api.user.service.UserService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Set;

import com.english.api.user.dto.request.ChangePasswordRequest;
import com.english.api.user.dto.request.UpdateUserRequest;
import com.english.api.user.dto.response.AdminUserResponse;
import com.english.api.user.dto.response.UserUpdateResponse;
import com.english.api.common.dto.PaginationResponse;

// import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
/**
 * Created by hungpham on 9/23/2025
 */
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
        UserMapper userMapper,
        @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public Optional<User> findOptionalByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<User> findOptionalByEmailWithRoles(String email) {
        return userRepository.findByEmailWithRoles(email);
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

        List<String> roles = rows.stream()
                .map(r -> (String) r[4])
                .filter(Objects::nonNull)
                .toList();

        return new UserResponse(id, email, fullName, avatarUrl, roles);
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

    @Override
    public UserUpdateResponse updateCurrentUser(UpdateUserRequest request) {
        User user = getCurrentUserEntity();
        // UUID userId = SecurityUtil.getCurrentUserId();
        if (request.getFullName() != null) setIfPresent(user, "FullName", request.getFullName());
        if (request.getEmail() != null) setIfPresent(user, "Email", request.getEmail());
        if (request.getPhone() != null) setIfPresent(user, "Phone", request.getPhone());
        userRepository.save(user);
        return new UserUpdateResponse(getId(user), getString(user, "FullName"), getString(user, "Email"), getString(user, "Phone"));
    }

    @Override
    public void updatePassword(ChangePasswordRequest request) {
        User user = getCurrentUserEntity();
        // naive: assume we can check old password via encoder matches
        String currentPassword = getString(user, "Password");
        if (currentPassword != null && !passwordEncoder.matches(request.getOldPassword(), currentPassword)) {
            throw new IllegalArgumentException("Old password is incorrect");
        }
        setIfPresent(user, "Password", passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public PaginationResponse getUsers(String searchTerm, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = userRepository.findAll(pageable);
        var items = users.getContent().stream().map(this::toAdminUserResponse).toList();
    
        PaginationResponse.Meta meta = new PaginationResponse.Meta(
            users.getNumber() + 1,
            users.getSize(),
            users.getTotalPages(),
            users.getTotalElements()
        );
        return new PaginationResponse(meta, items);
    }
    
    @Override
    public void toggleUserStatus(UUID userId, String lockReason) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        userRepository.save(user);
    }

    private User getCurrentUserEntity() {
        // Lấy ID từ SecurityUtil (nếu bạn đã có util lưu userId trong JWT/session)
        UUID userId = SecurityUtil.getCurrentUserId();
    
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }
    

    private AdminUserResponse toAdminUserResponse(User u) {
        // Chuyển đổi từ Set<Role> thành List<String> (danh sách các role codes)
        List<String> roleCodes = u.getRoles().stream()
                                  .map(role -> role.getCode())  // Giả sử `getCode()` trả về mã role (String)
                                  .collect(Collectors.toList());
    
        return new AdminUserResponse(
            getId(u),
            getString(u, "FullName"),
            getString(u, "Email"),
            !isLocked(u),
            getInstant(u, "CreatedAt"),
            roleCodes  // Truyền danh sách role codes vào
        );
    }

    private UUID getId(User u) {
        try { return (UUID) u.getClass().getMethod("getId").invoke(u); } catch (Exception e) { return null; }
    }

    private String getString(User u, String fieldCamel) {
        for (String prefix : new String[]{"get", "is"}) {
            try { return (String) u.getClass().getMethod(prefix + fieldCamel).invoke(u); } catch (Exception ignore) {}
        }
        return null;
    }

    private Instant getInstant(User u, String fieldCamel) {
        try { return (Instant) u.getClass().getMethod("get" + fieldCamel).invoke(u); } catch (Exception e) { return null; }
    }
    

    private void setIfPresent(User u, String fieldCamel, Object value) {
        for (Class<?> t : new Class[]{String.class, Object.class}) {
            try {
                var m = u.getClass().getMethod("set" + fieldCamel, t);
                m.invoke(u, value);
                return;
            } catch (Exception ignore) {}
        }
        // try boolean setter for active/enabled with primitive type
        try {
            var m = u.getClass().getMethod("set" + fieldCamel, boolean.class);
            m.invoke(u, value);
        } catch (Exception ignore) {}
    }

    private boolean isLocked(User u) {
        try { return !(boolean) u.getClass().getMethod("isAccountNonLocked").invoke(u); } catch (Exception ignore) {}
        try { return !(boolean) u.getClass().getMethod("isActive").invoke(u); } catch (Exception ignore) {}
        try { return !(boolean) u.getClass().getMethod("isEnabled").invoke(u); } catch (Exception ignore) {}
        return false;
    }

}
