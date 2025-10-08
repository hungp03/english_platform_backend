package com.english.api.course.mapper;

import com.english.api.course.dto.response.MediaAssetResponse;
import com.english.api.course.model.MediaAsset;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * Created by hungpham on 10/7/2025
 */
@Mapper(componentModel = "spring")
public interface MediaAssetMapper {

    MediaAssetMapper INSTANCE = Mappers.getMapper(MediaAssetMapper.class);
    MediaAssetResponse toResponse(MediaAsset entity);
    List<MediaAssetResponse> toResponseList(List<MediaAsset> entities);
}