package com.english.api.user.mapper;

import com.english.api.user.dto.response.LearningProfileResponse;
import com.english.api.user.model.LearningProfile;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LearningProfileMapper {
    LearningProfileResponse toResponse(LearningProfile profile);
}
