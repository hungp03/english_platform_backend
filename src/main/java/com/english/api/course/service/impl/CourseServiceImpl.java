package com.english.api.course.service.impl;

import com.english.api.auth.util.SecurityUtil;
import com.english.api.common.dto.PaginationResponse;
import com.english.api.common.exception.ResourceNotFoundException;
import com.english.api.common.exception.UnauthorizedException;
import com.english.api.course.dto.request.CourseRequest;
import com.english.api.course.dto.response.CourseDetailResponse;
import com.english.api.course.dto.response.CourseResponse;
import com.english.api.course.dto.response.CourseWithStatsResponse;
import com.english.api.course.mapper.CourseMapper;
import com.english.api.course.model.Course;
import com.english.api.course.repository.CourseRepository;
import com.english.api.course.service.CourseService;
import com.english.api.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Created by hungpham on 10/2/2025
 */
@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {
    private final CourseRepository courseRepository;
    private final CourseMapper mapper;

    @Override
    public CourseDetailResponse getById(UUID id) {
        return courseRepository.findDetailById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
    }

    @Transactional
    @Override
    public CourseResponse create(CourseRequest req) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        Course course = mapper.toEntity(req);
        course.setCreatedBy(User.builder().id(currentUserId).build());
        return mapper.toResponse(courseRepository.save(course));
    }

    @Override
    public PaginationResponse getCourses(Pageable pageable, String keyword, Boolean isPublished, String[] skills) {
        var page = courseRepository.searchWithStats(keyword, isPublished, skills, pageable)
                .map(projection -> new CourseWithStatsResponse(
                        projection.getId(),
                        projection.getTitle(),
                        projection.getDescription(),
                        projection.getLanguage(),
                        projection.getThumbnail(),
                        projection.getSkillFocus(),
                        projection.getPriceCents(),
                        projection.getCurrency(),
                        projection.getIsPublished(),
                        projection.getModuleCount(),
                        projection.getLessonCount(),
                        projection.getCreatedAt(),
                        projection.getUpdatedAt()
                ));

        return PaginationResponse.from(page, pageable);
    }

    @Override
    public PaginationResponse getCoursesForInstructor(Pageable pageable, String keyword, Boolean isPublished, String[] skills) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        var page = courseRepository.searchByOwnerWithStats(currentUserId, keyword, isPublished, skills, pageable)
                .map(projection -> new CourseWithStatsResponse(
                        projection.getId(),
                        projection.getTitle(),
                        projection.getDescription(),
                        projection.getLanguage(),
                        projection.getThumbnail(),
                        projection.getSkillFocus(),
                        projection.getPriceCents(),
                        projection.getCurrency(),
                        projection.getIsPublished(),
                        projection.getModuleCount(),
                        projection.getLessonCount(),
                        projection.getCreatedAt(),
                        projection.getUpdatedAt()
                ));
        return PaginationResponse.from(page, pageable);
    }

    @Transactional
    @Override
    public CourseResponse update(UUID id, CourseRequest req) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();

        UUID ownerId = courseRepository.findOwnerIdById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (!ownerId.equals(currentUserId)) {
            throw new UnauthorizedException("You are not allowed to update this course.");
        }

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        course.setTitle(req.title());
        course.setDescription(req.description());
        course.setDetailedDescription(req.detailedDescription());
        course.setLanguage(req.language());

        if (req.thumbnail() != null && !req.thumbnail().isBlank()) {
            course.setThumbnail(req.thumbnail());
        }

        if (req.skillFocus() != null) {
            course.setSkillFocus(req.skillFocus().toArray(new String[0]));
        }

        course.setPriceCents(req.priceCents());
        course.setCurrency(req.currency());

        return mapper.toResponse(courseRepository.save(course));
    }


    @Override
    @Transactional
    public void delete(UUID id) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();

        UUID ownerId = courseRepository.findOwnerIdById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (!ownerId.equals(currentUserId)) {
            throw new UnauthorizedException("You are not allowed to delete this course.");
        }

        courseRepository.softDeleteById(id, Instant.now());
    }


    @Transactional
    @Override
    public CourseResponse publish(UUID id, boolean publish) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        course.setPublished(publish);
        course.setPublishedAt(publish ? Instant.now() : null);
        return mapper.toResponse(courseRepository.save(course));
    }

    @Override
    public PaginationResponse getPublishedCourses(Pageable pageable, String keyword, String[] skills) {
        var page = courseRepository.searchWithStats(keyword, true, skills, pageable)
                .map(projection -> new CourseWithStatsResponse(
                        projection.getId(),
                        projection.getTitle(),
                        projection.getDescription(),
                        projection.getLanguage(),
                        projection.getThumbnail(),
                        projection.getSkillFocus(),
                        projection.getPriceCents(),
                        projection.getCurrency(),
                        projection.getIsPublished(),
                        projection.getModuleCount(),
                        projection.getLessonCount(),
                        projection.getCreatedAt(),
                        projection.getUpdatedAt()
                ));
        return PaginationResponse.from(page, pageable);
    }
}
