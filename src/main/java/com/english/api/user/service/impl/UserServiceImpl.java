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
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Set;

import com.english.api.user.dto.request.UpdatePasswordRequest;
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
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    
    private final PasswordEncoder passwordEncoder;

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
    public void updatePassword(UpdatePasswordRequest request) {
        User user = getCurrentUserEntity();
        // naive: assume we can check old password via encoder matches
        String currentPassword = getString(user, "Password");
        if (currentPassword != null && !passwordEncoder.matches(request.getOldPassword(), currentPassword)) {
            throw new IllegalArgumentException("Old password is incorrect");
        }
        setIfPresent(user, "Password", passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    // @Override
    // public PaginationResponse<AdminUserResponse> getUsers(String searchTerm, int page, int size) {
    //     Pageable pageable = PageRequest.of(page, size);
    //     Page<User> users = userRepository.findAll(pageable); // simple; could be filtered by searchTerm by customizing repository
    //     var items = users.getContent().stream().map(this::toAdminUserResponse).toList();
    //     return new PaginationResponse<>(items, users.getTotalElements(), page, size);
    // }
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

        // boolean nowLocked = toggleLockFlags(user);
        userRepository.save(user);

        // if (nowLocked) {
        //     String email = getString(user, "Email");
        //     if (email != null && !email.isBlank()) {
        //         mailService.sendAccountLockedEmail(email, lockReason);
        //     }
        // }
    }

    // ===== Helpers =====

    // private User getCurrentUserEntity() {
    //     Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    //     if (auth == null) throw new IllegalStateException("Not authenticated");
    //     String username = auth.getName();
    //     // Try by email first, fallback to username if repository has such method
    //     try {
    //         var m = userRepository.getClass().getMethod("findByEmail", String.class);
    //         var opt = (java.util.Optional<User>) m.invoke(userRepository, username);
    //         return opt.orElseThrow();
    //     } catch (Exception ignore) { }
    //     try {
    //         var m = userRepository.getClass().getMethod("findByUsername", String.class);
    //         var opt = (java.util.Optional<User>) m.invoke(userRepository, username);
    //         return opt.orElseThrow();
    //     } catch (Exception e) {
    //         throw new IllegalStateException("Cannot resolve current user by principal name");
    //     }
    // }
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

    // @SuppressWarnings("unchecked")
    // private Set<String> getRoles(User u) {
    //     try {
    //         var roles = (java.util.Set<?>) u.getClass().getMethod("getRoles").invoke(u);
    //         return (Set<String>) roles.stream().map(o -> {
    //             try {
    //                 return (String) o.getClass().getMethod("getName").invoke(o);
    //             } catch (Exception e) { return null; }
    //         }).filter(java.util.Objects::nonNull).collect(Collectors.toSet());
    //     } catch (Exception e) {
    //         return java.util.Set.of();
    //     }
    // }
    

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

    // private boolean toggleLockFlags(User u) {
    //     // prefer accountNonLocked=false as lock
    //     try {
    //         boolean current = (boolean) u.getClass().getMethod("isAccountNonLocked").invoke(u);
    //         u.getClass().getMethod("setAccountNonLocked", boolean.class).invoke(u, !current);
    //         return !(!current); // if we set to false => locked now
    //     } catch (Exception ignore) {}

    //     // else toggle active => false means locked
    //     try {
    //         boolean active = (boolean) u.getClass().getMethod("isActive").invoke(u);
    //         u.getClass().getMethod("setActive", boolean.class).invoke(u, !active);
    //         return !(!active) == false ? false : !active; // if set to false => locked
    //     } catch (Exception ignore) {}

    //     // else toggle enabled
    //     try {
    //         boolean enabled = (boolean) u.getClass().getMethod("isEnabled").invoke(u);
    //         u.getClass().getMethod("setEnabled", boolean.class).invoke(u, !enabled);
    //         return !enabled == false ? false : !enabled;
    //     } catch (Exception ignore) {}

    //     // default: no-op
    //     return false;
    // }

    private boolean isLocked(User u) {
        try { return !(boolean) u.getClass().getMethod("isAccountNonLocked").invoke(u); } catch (Exception ignore) {}
        try { return !(boolean) u.getClass().getMethod("isActive").invoke(u); } catch (Exception ignore) {}
        try { return !(boolean) u.getClass().getMethod("isEnabled").invoke(u); } catch (Exception ignore) {}
        return false;
    }


}
