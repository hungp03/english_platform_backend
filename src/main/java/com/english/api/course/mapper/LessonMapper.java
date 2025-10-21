package com.english.api.course.mapper;

/**
 * Created by hungpham on 10/7/2025
 */
import com.english.api.course.dto.request.LessonRequest;
import com.english.api.course.dto.response.LessonResponse;
import com.english.api.course.model.CourseModule;
import com.english.api.course.model.Lesson;
import com.english.api.course.model.enums.LessonMediaRole;
import com.english.api.course.model.MediaAsset;
import org.mapstruct.*;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface LessonMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "module", source = "module")
    @Mapping(source = "request.title", target = "title")
    @Mapping(source = "request.kind", target = "kind")
    @Mapping(source = "request.estimatedMin", target = "estimatedMin")
    @Mapping(source = "request.position", target = "position")
    @Mapping(source = "request.isFree", target = "isFree")
    @Mapping(source = "request.content", target = "content")
    default Lesson toEntity(LessonRequest request, CourseModule module) {
        Lesson lesson = new Lesson();
        lesson.setModule(module);
        lesson.setTitle(request.title());
        lesson.setKind(request.kind());
        lesson.setEstimatedMin(request.estimatedMin());
        lesson.setPosition(request.position());
        lesson.setIsFree(request.isFree());
        lesson.setContent(request.content());
        return lesson;
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "module", ignore = true)
    @Mapping(target = "position", ignore = true)
    void updateFromRequest(LessonRequest request, @MappingTarget Lesson lesson);

    // Convert entity -> response
    default LessonResponse toResponse(Lesson lesson) {
        UUID primaryId = lesson.getPrimaryMedia().map(MediaAsset::getId).orElse(null);
        List<UUID> attachmentIds = lesson.getMediaLinks().stream()
                .filter(lm -> lm.getRole() == LessonMediaRole.ATTACHMENT)
                .map(lm -> lm.getMedia().getId())
                .toList();

        return new LessonResponse(
                lesson.getId(),
                lesson.getTitle(),
                lesson.getKind(),
                lesson.getEstimatedMin(),
                lesson.getPosition(),
                lesson.getIsFree(),
                lesson.getPublished(),
                lesson.getContent(),
                primaryId,
                attachmentIds
        );
    }
}

