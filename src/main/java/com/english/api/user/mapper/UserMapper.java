package com.english.api.user.mapper;

import com.english.api.user.dto.response.ListUserResponse;
import com.english.api.user.dto.response.UserUpdateResponse;
import com.english.api.user.model.User;
import org.mapstruct.Mapper;

/**
 * Created by hungpham on 9/24/2025
 */
@Mapper(componentModel = "spring")
public interface UserMapper {
    UserUpdateResponse toUpdateResponse(User user);
    ListUserResponse toListUserResponse(User user);
}
