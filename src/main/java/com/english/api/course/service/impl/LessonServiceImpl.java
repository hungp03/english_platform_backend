package com.english.api.course.service.impl;

import com.english.api.common.exception.DuplicatePositionException;
import com.english.api.common.exception.ResourceNotFoundException;
import com.english.api.course.dto.request.LessonRequest;
import com.english.api.course.dto.response.LessonResponse;
import com.english.api.course.mapper.LessonMapper;
import com.english.api.course.model.CourseModule;
import com.english.api.course.model.Lesson;
import com.english.api.course.model.MediaAsset;
import com.english.api.course.repository.CourseModuleRepository;
import com.english.api.course.repository.LessonRepository;
import com.english.api.course.repository.MediaAssetRepository;
import com.english.api.course.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Created by hungpham on 10/7/2025
 */
@Service
@RequiredArgsConstructor
public class LessonServiceImpl implements LessonService {
    private final CourseModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final MediaAssetRepository assetRepository;
    private final LessonMapper lessonMapper;

    @Override
    public LessonResponse create(UUID moduleId, LessonRequest request) {
        CourseModule module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found"));

        // auto position if null
        Integer position = request.position();
        if (position == null) {
            position = lessonRepository.findMaxPositionByModuleId(moduleId).orElse(0) + 1;
        }

        // check duplicate
        if (lessonRepository.existsByModuleIdAndPosition(moduleId, position)) {
            throw new DuplicatePositionException(
                    String.format("Lesson position %d already exists in this module", position)
            );
        }

        Lesson lesson = lessonMapper.toEntity(request, module);
        lesson.setPosition(position);
        lessonRepository.save(lesson);

        return lessonMapper.toResponse(lesson);
    }


    @Override
    public List<LessonResponse> list(UUID moduleId) {
        return lessonRepository.findByModuleIdOrderByPosition(moduleId).stream()
                .map(lessonMapper::toResponse)
                .toList();
    }

    // --- Get by ID ---
    @Override
    public LessonResponse getById(UUID moduleId, UUID lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .filter(l -> l.getModule().getId().equals(moduleId))
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));
        return lessonMapper.toResponse(lesson);
    }

    // --- Update ---
    @Override
    public LessonResponse update(UUID moduleId, UUID lessonId, LessonRequest request) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .filter(l -> l.getModule().getId().equals(moduleId))
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));

        Integer newPosition = request.position();

        // Nếu người dùng gửi position mới và nó khác position hiện tại
        if (newPosition != null && !newPosition.equals(lesson.getPosition())) {
            boolean exists = lessonRepository.existsByModuleIdAndPosition(moduleId, newPosition);

            if (exists) {
                throw new DuplicatePositionException(
                        String.format("Lesson position %d already exists in this module", newPosition)
                );
            }

            lesson.setPosition(newPosition);
        }

        // 🧩 Update các field còn lại bằng MapStruct
        lessonMapper.updateFromRequest(request, lesson);

        lessonRepository.save(lesson);
        return lessonMapper.toResponse(lesson);
    }


    // --- Delete ---
    @Override
    public void delete(UUID moduleId, UUID lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .filter(l -> l.getModule().getId().equals(moduleId))
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));
        lessonRepository.delete(lesson);
    }

    // --- Attach Asset ---
    @Override
    public LessonResponse attachAsset(UUID lessonId, UUID assetId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));
        MediaAsset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found"));

        lesson.getAssets().add(asset);
        lessonRepository.save(lesson);
        return lessonMapper.toResponse(lesson);
    }

    // --- Detach Asset ---
    @Override
    public LessonResponse detachAsset(UUID lessonId, UUID assetId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));

        boolean removed = lesson.getAssets().removeIf(a -> a.getId().equals(assetId));
        if (!removed) throw new ResourceNotFoundException("Asset not attached to this lesson");

        lessonRepository.save(lesson);
        return lessonMapper.toResponse(lesson);
    }
}
