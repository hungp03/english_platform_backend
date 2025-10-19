package com.english.api.course.service.impl;

import com.english.api.auth.util.SecurityUtil;
import com.english.api.common.exception.DuplicatePositionException;
import com.english.api.common.exception.ResourceInvalidException;
import com.english.api.common.exception.ResourceNotFoundException;
import com.english.api.common.exception.UnauthorizedException;
import com.english.api.course.dto.request.LessonRequest;
import com.english.api.course.dto.response.LessonResponse;
import com.english.api.course.dto.response.LessonSummaryResponse;
import com.english.api.course.mapper.LessonMapper;
import com.english.api.course.model.*;
import com.english.api.course.repository.*;
import com.english.api.course.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by hungpham on 10/7/2025
 */
@Service
@RequiredArgsConstructor
public class LessonServiceImpl implements LessonService {
    private final CourseRepository courseRepository;
    private final CourseModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final MediaAssetRepository assetRepository;
    private final LessonMediaRepository lessonMediaRepository;
    private final LessonMapper lessonMapper;

    // --- CREATE ---
    @Override
    @Transactional
    public LessonResponse create(UUID moduleId, LessonRequest request) {
        CourseModule module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found"));

        // Auto position nếu null
        Integer position = Optional.ofNullable(request.position())
                .orElseGet(() -> lessonRepository.findMaxPositionByModuleId(moduleId).orElse(0) + 1);

        // Check trùng position trong module
        if (lessonRepository.existsByModuleIdAndPosition(moduleId, position)) {
            throw new DuplicatePositionException(
                    String.format("Lesson position %d already exists in this module", position)
            );
        }

        Lesson lesson = lessonMapper.toEntity(request, module);
        lesson.setPosition(position);
        lessonRepository.save(lesson);

        // Nếu có mediaId → thêm link PRIMARY
        if (request.mediaId() != null) {
            MediaAsset media = assetRepository.findById(request.mediaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Media not found"));
            LessonMedia link = LessonMedia.builder()
                    .lesson(lesson)
                    .media(media)
                    .role(LessonMediaRole.PRIMARY)
                    .position(0)
                    .build();
            lessonMediaRepository.save(link);
        }

        return lessonMapper.toResponse(lesson);
    }

    // --- LIST ---
    @Override
    public List<LessonSummaryResponse> list(UUID moduleId) {
        return lessonRepository.findSummaryByModuleId(moduleId);
    }

    @Override
    public List<LessonSummaryResponse> listPublished(UUID moduleId) {
        return lessonRepository.findPublishedSummaryByModuleId(moduleId);
    }


    // --- GET ---
    @Override
    public LessonResponse getById(UUID moduleId, UUID lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .filter(l -> l.getModule().getId().equals(moduleId))
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));
        return lessonMapper.toResponse(lesson);
    }

    // --- UPDATE ---
    @Override
    @Transactional
    public LessonResponse update(UUID moduleId, UUID lessonId, LessonRequest request) {
        validateCourseOwnershipByLesson(lessonId);
        Lesson lesson = lessonRepository.findById(lessonId)
                .filter(l -> l.getModule().getId().equals(moduleId))
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));

        Integer newPosition = request.position();

        // Check trùng position
        if (newPosition != null && !newPosition.equals(lesson.getPosition())) {
            boolean exists = lessonRepository.existsByModuleIdAndPosition(moduleId, newPosition);
            if (exists) {
                throw new DuplicatePositionException(
                        String.format("Lesson position %d already exists in this module", newPosition)
                );
            }
            lesson.setPosition(newPosition);
        }

        // Update field khác
        lessonMapper.updateFromRequest(request, lesson);

        // Xử lý media chính
        UUID mediaId = request.mediaId();
        Optional<LessonMedia> currentPrimary = lessonMediaRepository
                .findByLessonIdAndRole(lessonId, LessonMediaRole.PRIMARY);

        if (mediaId != null) {
            MediaAsset media = assetRepository.findById(mediaId)
                    .orElseThrow(() -> new ResourceNotFoundException("Media not found"));
            if (currentPrimary.isEmpty()) {
                LessonMedia newLink = LessonMedia.builder()
                        .lesson(lesson)
                        .media(media)
                        .role(LessonMediaRole.PRIMARY)
                        .position(0)
                        .build();
                lessonMediaRepository.save(newLink);
            } else {
                LessonMedia old = currentPrimary.get();
                if (!old.getMedia().getId().equals(mediaId)) {
                    lessonMediaRepository.delete(old);
                    lessonMediaRepository.save(LessonMedia.builder()
                            .lesson(lesson)
                            .media(media)
                            .role(LessonMediaRole.PRIMARY)
                            .position(0)
                            .build());
                }
            }
        } else {
            // Gỡ media chính nếu client gửi null
            currentPrimary.ifPresent(lessonMediaRepository::delete);
        }

        lessonRepository.save(lesson);
        return lessonMapper.toResponse(lesson);
    }

    // --- DELETE ---
    @Override
    public void delete(UUID moduleId, UUID lessonId) {
        validateCourseOwnershipByLesson(lessonId);
        Lesson lesson = lessonRepository.findById(lessonId)
                .filter(l -> l.getModule().getId().equals(moduleId))
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));
        lessonRepository.delete(lesson);
    }

    // --- ATTACH ASSET ---
    @Override
    public LessonResponse attachAsset(UUID lessonId, UUID assetId) {
        validateCourseOwnershipByLesson(lessonId);
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));
        MediaAsset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found"));

        boolean exists = lessonMediaRepository.existsByLessonIdAndMediaId(lessonId, assetId);
        if (!exists) {
            int nextPos = lessonMediaRepository.findByLessonIdOrderByPositionAsc(lessonId).size() + 1;
            LessonMedia link = LessonMedia.builder()
                    .lesson(lesson)
                    .media(asset)
                    .role(LessonMediaRole.ATTACHMENT)
                    .position(nextPos)
                    .build();
            lessonMediaRepository.save(link);
        }

        return lessonMapper.toResponse(lesson);
    }

    // --- DETACH ASSET ---
    @Override
    public LessonResponse detachAsset(UUID lessonId, UUID assetId) {
        validateCourseOwnershipByLesson(lessonId);
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));

        LessonMedia link = lessonMediaRepository.findByLessonIdAndMediaId(lessonId, assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not attached to this lesson"));

        if (link.getRole() == LessonMediaRole.PRIMARY) {
            throw new ResourceInvalidException("Cannot detach primary media via this endpoint");
        }

        lessonMediaRepository.delete(link);
        return lessonMapper.toResponse(lesson);
    }

    private void validateCourseOwnershipByLesson(UUID lessonId) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();

        // Truy vấn trực tiếp chủ sở hữu của course qua lesson
        UUID ownerId = courseRepository.findOwnerIdByLessonId(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found for this lesson"));

        if (!ownerId.equals(currentUserId)) {
            throw new UnauthorizedException("You are not allowed to modify this lesson.");
        }
    }

    // --- PUBLISH ---
    @Override
    @Transactional
    public LessonResponse publish(UUID moduleId, UUID lessonId, boolean publish) {
        validateCourseOwnershipByLesson(lessonId);
        Lesson lesson = lessonRepository.findById(lessonId)
                .filter(l -> l.getModule().getId().equals(moduleId))
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));

        lesson.setPublished(publish);
        lessonRepository.save(lesson);

        return lessonMapper.toResponse(lesson);
    }

}
