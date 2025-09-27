package com.english.api.user.mapper;

import com.english.api.user.dto.response.UserResponse;
import com.english.api.user.model.Role;
import com.english.api.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Set;

/**
 * Created by hungpham on 9/24/2025
 */
@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "roles", expression = "java(mapRoleCodes(user.getRoles()))")
    UserResponse toUserResponse(User user);

    default List<String> mapRoleCodes(Set<Role> roles) {
        if (roles == null) {
            return List.of();
        }
        return roles.stream()
                .map(Role::getCode)
                .toList();
    }
}
