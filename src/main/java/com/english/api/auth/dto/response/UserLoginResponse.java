package com.english.api.auth.dto.response;

import com.english.api.user.model.User;

import java.util.List;
import java.util.UUID;

/**
 * Created by hungpham on 9/22/2025
 */
public record UserLoginResponse(
        UUID userId,
        String email,
        String fullName,
        List<RoleResponse> roles
) {
    public static UserLoginResponse from(User user) {
        return new UserLoginResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRoles().stream()
                        .map(role -> new RoleResponse(
                                role.getId(),
                                role.getName()
                        ))
                        .toList()
        );
    }

    public record RoleResponse(
            UUID roleId,
            String roleName
    ) {
    }
}
