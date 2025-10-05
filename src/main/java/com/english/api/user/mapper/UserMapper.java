package com.english.api.user.mapper;

import com.english.api.user.dto.response.ListUserResponse;
import com.english.api.user.dto.response.UserUpdateResponse;
import com.english.api.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Created by hungpham on 9/24/2025
 */
@Mapper(componentModel = "spring")
public interface UserMapper {
    UserUpdateResponse toUpdateResponse(User user);
    @Mapping(target = "isActive", source = "active")
    ListUserResponse toListUserResponse(User user);
}
