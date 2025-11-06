package com.english.api.course.service.impl;

import com.english.api.auth.util.SecurityUtil;
import com.english.api.common.exception.*;
import com.english.api.common.service.MediaService;
import com.english.api.course.dto.request.LessonRequest;
import com.english.api.course.dto.response.LessonResponse;
import com.english.api.course.dto.response.LessonSummaryResponse;
import com.english.api.course.mapper.LessonMapper;
import com.english.api.course.model.*;
import com.english.api.course.model.enums.LessonMediaRole;
import com.english.api.course.repository.*;
import com.english.api.course.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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
    private final MediaService mediaService;

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

        // Cập nhật vị trí nếu có thay đổi
        Integer newPosition = request.position();
        if (newPosition != null && !newPosition.equals(lesson.getPosition())) {
            boolean exists = lessonRepository.existsByModuleIdAndPosition(moduleId, newPosition);
            if (exists) {
                throw new DuplicatePositionException(
                        String.format("Lesson position %d already exists in this module", newPosition)
                );
            }
            lesson.setPosition(newPosition);
        }

        // Cập nhật các trường khác
        lessonMapper.updateFromRequest(request, lesson);

        // Xử lý media chính
        UUID mediaId = request.mediaId();
        if (mediaId != null) {
            // Xóa media cũ (transaction riêng, commit xong trước)
            removePrimaryMedia(lessonId);

            // Thêm media mới
            MediaAsset newMedia = assetRepository.findById(mediaId)
                    .orElseThrow(() -> new ResourceNotFoundException("Media not found"));

            LessonMedia newPrimary = LessonMedia.builder()
                    .lesson(lesson)
                    .media(newMedia)
                    .role(LessonMediaRole.PRIMARY)
                    .position(0)
                    .build();

            lesson.getMediaLinks().add(newPrimary);
        } else {
            // Nếu client gửi null → chỉ cần xóa media cũ
            removePrimaryMedia(lessonId);
        }

        // Lưu lesson (Hibernate sẽ tự insert LessonMedia mới)
        lessonRepository.save(lesson);
        return lessonMapper.toResponse(lesson);
    }

//    ALTER TABLE IF EXISTS public.lesson_media DROP CONSTRAINT IF EXISTS fk_lesson_media_asset;
//    ALTER TABLE IF EXISTS public.lesson_media
//    ADD CONSTRAINT fk_lesson_media_asset FOREIGN KEY (media_id)
//    REFERENCES public.media_assets (id) MATCH SIMPLE
//    ON UPDATE NO ACTION
//    ON DELETE CASCADE;
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void removePrimaryMedia(UUID lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));

        // Lấy danh sách LessonMedia đóng vai trò PRIMARY
        List<LessonMedia> toRemove = lesson.getMediaLinks().stream()
                .filter(m -> m.getRole() == LessonMediaRole.PRIMARY)
                .toList();

        if (!toRemove.isEmpty()) {
            for (LessonMedia lm : toRemove) {
                MediaAsset asset = lm.getMedia();
                String fileUrl = asset.getUrl();

                // Xóa file vật lý (nếu có)
                if (fileUrl != null && !fileUrl.isBlank()) {
                    mediaService.deleteFileByUrl(fileUrl);
                }

                // Xóa luôn MediaAsset (Hibernate sẽ tự xóa LessonMedia vì cascade REMOVE)
                assetRepository.delete(asset);
            }

            // Flush để đảm bảo commit transaction xóa trước khi thêm mới
            lessonRepository.flush();
        }
    }


    // --- DELETE ---
    @Override
    @Transactional
    public void delete(UUID moduleId, UUID lessonId) {
        validateCourseOwnershipByLesson(lessonId);

        Lesson lesson = lessonRepository.findById(lessonId)
                .filter(l -> l.getModule().getId().equals(moduleId))
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));

        // Xóa tất cả media và file vật lý trước khi xóa lesson
        if (!lesson.getMediaLinks().isEmpty()) {
            for (LessonMedia lm : lesson.getMediaLinks()) {
                MediaAsset asset = lm.getMedia();
                if (asset != null) {
                    String url = asset.getUrl();

                    // Xóa file thật (nếu có)
                    if (url != null && !url.isBlank()) {
                        try {
                            mediaService.deleteFileByUrl(url);
                        } catch (Exception ignored) {
                        }
                    }

                    // Xóa MediaAsset trong DB
                    try {
                        assetRepository.delete(asset);
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        // Hibernate sẽ tự xóa LessonMedia nhờ cascade + orphanRemoval
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
            throw new AccessDeniedException("You are not allowed to modify this lesson.");
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
