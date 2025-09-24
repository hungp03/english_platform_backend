package com.english.api.user.mapper;

import com.english.api.user.dto.response.UserResponse;
import com.english.api.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * Created by hungpham on 9/24/2025
 */
@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    UserResponse toUserResponse(User user);
}
