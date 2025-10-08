package com.english.api.course.mapper;

import com.english.api.course.dto.request.LessonBlockRequest;
import com.english.api.course.dto.response.LessonBlockResponse;
import com.english.api.course.model.LessonBlock;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Created by hungpham on 10/8/2025
 */
@Mapper(componentModel = "spring", uses = {MediaAssetMapper.class})
public interface LessonBlockMapper {

    LessonBlockResponse toResponse(LessonBlock entity);

    List<LessonBlockResponse> toResponseList(List<LessonBlock> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lesson", ignore = true)
    @Mapping(target = "media", ignore = true)
    LessonBlock toEntity(LessonBlockRequest dto);
}

