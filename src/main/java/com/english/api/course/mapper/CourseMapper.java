package com.english.api.course.mapper;

import com.english.api.course.dto.request.CourseRequest;
import com.english.api.course.dto.response.CourseResponse;
import com.english.api.course.model.Course;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Arrays;

/**
 * Created by hungpham on 10/2/2025
 */
@Mapper(componentModel = "spring", imports = Arrays.class)
public interface CourseMapper {
    @Mapping(target = "skillFocus", expression = "java(req.skillFocus() != null ? req.skillFocus().toArray(new String[0]) : null)")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "publishedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "status", constant = "DRAFT")
    Course toEntity(CourseRequest req);

    @Mapping(target = "skillFocus", expression = "java(c.getSkillFocus() != null ? Arrays.asList(c.getSkillFocus()) : null)")
    CourseResponse toResponse(Course c);
}
