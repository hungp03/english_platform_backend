package com.english.api.course.service;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.course.dto.request.CourseRequest;
import com.english.api.course.dto.response.CourseCheckoutResponse;
import com.english.api.course.dto.response.CourseDetailResponse;
import com.english.api.course.dto.response.CourseResponse;
import com.english.api.course.dto.response.InstructorStatsResponse;
import com.english.api.course.dto.response.MonthlyGrowthResponse;
import com.english.api.course.model.enums.CourseStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import com.english.api.course.model.Course;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by hungpham on 10/2/2025
 */
public interface CourseService {
    Optional<Course> findPublishedById(UUID id);

    CourseDetailResponse getById(UUID id);

    CourseDetailResponse getPublishedBySlug(String slug);
    
    CourseDetailResponse getBySlugForAdmin(String slug);

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

    PaginationResponse getPublishedByInstructor(UUID instructorId, Pageable pageable, String keyword, String[] skills);

    CourseCheckoutResponse getCheckoutInfoById(UUID id);
    
    InstructorStatsResponse getInstructorStats(UUID instructorId);
    
    /**
     * Get monthly growth statistics for an instructor
     * Revenue and student count broken down by weekly periods (7-day intervals)
     * @param instructorId the instructor's user ID
     * @param year the year (e.g., 2025)
     * @param month the month (1-12)
     * @return monthly growth statistics with weekly breakdowns
     */
    MonthlyGrowthResponse getMonthlyGrowth(UUID instructorId, Integer year, Integer month);
}
