package com.english.api.course.service;

import com.english.api.course.dto.request.LessonRequest;
import com.english.api.course.dto.response.LessonResponse;

import java.util.List;
import java.util.UUID;

/**
 * Created by hungpham on 10/7/2025
 */
public interface LessonService {
    LessonResponse create(UUID moduleId, LessonRequest request);

    List<LessonResponse> list(UUID moduleId);

    // --- Get by ID ---
    LessonResponse getById(UUID moduleId, UUID lessonId);

    // --- Update ---
    LessonResponse update(UUID moduleId, UUID lessonId, LessonRequest request);

    // --- Delete ---
    void delete(UUID moduleId, UUID lessonId);

    // --- Attach Asset ---
    LessonResponse attachAsset(UUID lessonId, UUID assetId);

    // --- Detach Asset ---
    LessonResponse detachAsset(UUID lessonId, UUID assetId);
}
