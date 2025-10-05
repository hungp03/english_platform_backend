package com.english.api.course.service.impl;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.common.exception.ResourceNotFoundException;
import com.english.api.course.dto.request.CourseRequest;
import com.english.api.course.dto.response.CourseResponse;
import com.english.api.course.mapper.CourseMapper;
import com.english.api.course.model.Course;
import com.english.api.course.repository.CourseRepository;
import com.english.api.course.service.CourseService;
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
    public CourseResponse getById(UUID id) {
        return courseRepository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
    }

    @Transactional
    @Override
    public CourseResponse create(CourseRequest req) {
        Course course = mapper.toEntity(req);
        return mapper.toResponse(courseRepository.save(course));
    }

    @Override
    public PaginationResponse getCourses(Pageable pageable, String keyword, Boolean isPublished) {
        Page<Course> page;
        if ((keyword == null || keyword.isBlank()) && isPublished == null) {
            // no filter
            page = courseRepository.findAll(pageable);
        } else {
            // use native query with unaccent + publish
            page = courseRepository.search(keyword, isPublished, pageable);
        }

        return PaginationResponse.from(page, pageable);
    }

    @Transactional
    @Override
    public CourseResponse update(UUID id, CourseRequest req) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        course.setTitle(req.title());
        course.setDescription(req.description());
        course.setLanguage(req.language());
        if (req.skillFocus() != null) {
            course.setSkillFocus(req.skillFocus().toArray(new String[0]));
        }
        course.setPriceCents(req.priceCents());
        course.setCurrency(req.currency());

        return mapper.toResponse(courseRepository.save(course));
    }

    @Override
    public void delete(UUID id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));
        course.setDeletedAt(Instant.now());
        course.setDeleted(true);
        courseRepository.save(course);
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
}
