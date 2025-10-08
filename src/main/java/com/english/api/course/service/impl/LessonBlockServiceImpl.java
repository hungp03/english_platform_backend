package com.english.api.course.service.impl;

import com.english.api.common.exception.DuplicatePositionException;
import com.english.api.common.exception.ResourceNotFoundException;
import com.english.api.course.dto.request.LessonBlockRequest;
import com.english.api.course.dto.response.LessonBlockResponse;
import com.english.api.course.mapper.LessonBlockMapper;
import com.english.api.course.model.Lesson;
import com.english.api.course.model.LessonBlock;
import com.english.api.course.model.MediaAsset;
import com.english.api.course.repository.LessonBlockRepository;
import com.english.api.course.repository.LessonRepository;
import com.english.api.course.repository.MediaAssetRepository;
import com.english.api.course.service.LessonBlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Created by hungpham on 10/8/2025
 */
@Service
@RequiredArgsConstructor
public class LessonBlockServiceImpl implements LessonBlockService {
    private final LessonRepository lessonRepository;
    private final LessonBlockRepository blockRepository;
    private final MediaAssetRepository assetRepository;
    private final LessonBlockMapper mapper;

    @Override
    public List<LessonBlockResponse> list(UUID lessonId) {
        return mapper.toResponseList(blockRepository.findByLessonIdOrderByPosition(lessonId));
    }

    @Override
    public LessonBlockResponse getById(UUID lessonId, UUID blockId) {
        LessonBlock block = blockRepository.findById(blockId)
                .filter(b -> b.getLesson().getId().equals(lessonId))
                .orElseThrow(() -> new ResourceNotFoundException("Lesson block not found"));
        return mapper.toResponse(block);
    }

    @Override
    public LessonBlockResponse create(UUID lessonId, LessonBlockRequest req) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));

        // Auto position if not provided
        Integer position = req.position();
        if (position == null) {
            position = blockRepository.findMaxPositionByLessonId(lessonId).orElse(0) + 1;
        }

        // Check duplicate position
        if (blockRepository.existsByLessonIdAndPosition(lessonId, position)) {
            throw new DuplicatePositionException(
                    String.format("Block position %d already exists in this lesson", position)
            );
        }

        LessonBlock block = mapper.toEntity(req);
        block.setLesson(lesson);
        block.setPosition(position);

        if (req.mediaId() != null) {
            MediaAsset media = assetRepository.findById(req.mediaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Media not found"));
            block.setMedia(media);
        }

        blockRepository.save(block);
        return mapper.toResponse(block);
    }


    @Override
    public LessonBlockResponse update(UUID lessonId, UUID blockId, LessonBlockRequest req) {
        LessonBlock block = blockRepository.findById(blockId)
                .filter(b -> b.getLesson().getId().equals(lessonId))
                .orElseThrow(() -> new ResourceNotFoundException("Lesson block not found"));

        Integer newPosition = req.position();

        if (newPosition != null && !newPosition.equals(block.getPosition())) {
            boolean exists = blockRepository.existsByLessonIdAndPosition(lessonId, newPosition);
            if (exists) {
                throw new DuplicatePositionException(
                        String.format("Block position %d already exists in this lesson", newPosition)
                );
            }
            block.setPosition(newPosition);
        }

        // Update other fields
        block.setBlockType(req.blockType());
        block.setPayload(req.payload());

        if (req.mediaId() != null) {
            MediaAsset media = assetRepository.findById(req.mediaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Media not found"));
            block.setMedia(media);
        } else {
            block.setMedia(null);
        }

        blockRepository.save(block);
        return mapper.toResponse(block);
    }


    @Override
    public void delete(UUID lessonId, UUID blockId) {
        LessonBlock block = blockRepository.findById(blockId)
                .filter(b -> b.getLesson().getId().equals(lessonId))
                .orElseThrow(() -> new ResourceNotFoundException("Lesson block not found"));
        blockRepository.delete(block);
    }
}
