package com.english.api.course.service;

import com.english.api.course.dto.request.LessonBlockRequest;
import com.english.api.course.dto.response.LessonBlockResponse;

import java.util.List;
import java.util.UUID;

/**
 * Created by hungpham on 10/8/2025
 */
public interface LessonBlockService {
    List<LessonBlockResponse> list(UUID lessonId);

    LessonBlockResponse getById(UUID lessonId, UUID blockId);

    LessonBlockResponse create(UUID lessonId, LessonBlockRequest req);

    LessonBlockResponse update(UUID lessonId, UUID blockId, LessonBlockRequest req);

    void delete(UUID lessonId, UUID blockId);
}
