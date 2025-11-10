package com.english.api.user.mapper;

import com.english.api.user.dto.response.InstructorProfileResponse;
import com.english.api.user.model.InstructorProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for InstructorProfile entity
 * Created by hungpham on 10/29/2025
 */
@Mapper(componentModel = "spring")
public interface InstructorProfileMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.fullName", target = "fullName")
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "user.avatarUrl", target = "avatarUrl")
    InstructorProfileResponse toResponse(InstructorProfile instructorProfile);
}
