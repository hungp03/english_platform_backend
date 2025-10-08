package com.english.api.course.mapper;

import com.english.api.course.dto.request.CourseModuleRequest;
import com.english.api.course.dto.response.CourseModuleResponse;
import com.english.api.course.dto.response.CourseModuleUpdateResponse;
import com.english.api.course.model.CourseModule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Created by hungpham on 10/4/2025
 */
@Mapper(componentModel = "spring")
public interface CourseModuleMapper {

    CourseModuleResponse toResponse(CourseModule entity);
    CourseModuleUpdateResponse toUpdateResponse(CourseModule entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "course", ignore = true) // set trong service
    @Mapping(target = "position", ignore = true)
    CourseModule toEntity(CourseModuleRequest request);
}

