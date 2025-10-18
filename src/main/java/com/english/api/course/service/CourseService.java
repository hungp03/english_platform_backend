package com.english.api.course.service;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.course.dto.request.CourseRequest;
import com.english.api.course.dto.response.CourseDetailResponse;
import com.english.api.course.dto.response.CourseResponse;
import com.english.api.course.dto.response.CourseWithStatsResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Created by hungpham on 10/2/2025
 */
public interface CourseService {
    CourseDetailResponse getById(UUID id);

    @Transactional
    CourseResponse create(CourseRequest req);

    PaginationResponse getCourses(Pageable pageable, String keyword, Boolean isPublished);

    PaginationResponse getCoursesForInstructor(Pageable pageable, String keyword, Boolean isPublished);

    @Transactional
    CourseResponse update(UUID id, CourseRequest req);

    void delete(UUID id);

    @Transactional
    CourseResponse publish(UUID id, boolean publish);
}
