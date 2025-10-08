package com.english.api.course.mapper;

/**
 * Created by hungpham on 10/7/2025
 */
import com.english.api.course.dto.request.LessonRequest;
import com.english.api.course.dto.response.LessonResponse;
import com.english.api.course.model.CourseModule;
import com.english.api.course.model.Lesson;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface LessonMapper {
    LessonMapper INSTANCE = Mappers.getMapper(LessonMapper.class);
    LessonResponse toResponse(Lesson lesson);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "assets", ignore = true)
    @Mapping(target = "module", source = "module")
    @Mapping(source = "request.title", target = "title")
    @Mapping(source = "request.kind", target = "kind")
    @Mapping(source = "request.estimatedMin", target = "estimatedMin")
    @Mapping(source = "request.position", target = "position")
    @Mapping(source = "request.isFree", target = "isFree")
    Lesson toEntity(LessonRequest request, CourseModule module);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "module", ignore = true)
    @Mapping(target = "position", ignore = true)
    void updateFromRequest(LessonRequest request, @MappingTarget Lesson lesson);
}
