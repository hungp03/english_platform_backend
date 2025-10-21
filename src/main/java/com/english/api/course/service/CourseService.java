package com.english.api.course.service;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.course.dto.request.CourseRequest;
import com.english.api.course.dto.response.CourseDetailResponse;
import com.english.api.course.dto.response.CourseResponse;
import com.english.api.course.model.enums.CourseStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Created by hungpham on 10/2/2025
 */
public interface CourseService {
    CourseDetailResponse getById(UUID id);

    CourseDetailResponse getPublishedBySlug(String slug);

    @Transactional
    CourseResponse create(CourseRequest req);

    PaginationResponse getCourses(Pageable pageable, String keyword, String status, String[] skills);

    PaginationResponse getCoursesForInstructor(Pageable pageable, String keyword, String status, String[] skills);

    @Transactional
    CourseResponse update(UUID id, CourseRequest req);

    void delete(UUID id);

    @Transactional
    CourseResponse changeStatus(UUID id, CourseStatus status);

    PaginationResponse getPublishedCourses(Pageable pageable, String keyword, String[] skills);
}
