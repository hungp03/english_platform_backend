package com.english.api.course.mapper;

import com.english.api.course.dto.request.CourseRequest;
import com.english.api.course.dto.response.CourseResponse;
import com.english.api.course.model.Course;
import com.english.api.course.model.Skill;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by hungpham on 10/2/2025
 */
@Mapper(componentModel = "spring")
public interface CourseMapper {
    @Mapping(target = "skills", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    @Mapping(target = "publishedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "status", constant = "DRAFT")
    Course toEntity(CourseRequest req);

    @Mapping(target = "skillFocus", expression = "java(mapSkillsToList(c.getSkills()))")
    CourseResponse toResponse(Course c);
    
    default List<String> mapSkillsToList(Set<Skill> skills) {
        if (skills == null) return null;
        return skills.stream()
                .map(Skill::getName)
                .collect(Collectors.toList());
    }
}
