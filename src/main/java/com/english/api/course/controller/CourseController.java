package com.english.api.course.controller;

import com.english.api.auth.util.SecurityUtil;
import com.english.api.common.dto.PaginationResponse;
import com.english.api.course.dto.request.CourseRequest;
import com.english.api.course.dto.response.CourseCheckoutResponse;
import com.english.api.course.dto.response.CourseDetailResponse;
import com.english.api.course.dto.response.CourseResponse;
import com.english.api.course.dto.response.InstructorStatsResponse;
import com.english.api.course.dto.response.MonthlyGrowthResponse;
import com.english.api.course.model.enums.CourseStatus;
import com.english.api.course.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    // Get all courses (with pagination, keyword, status filter)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaginationResponse> getCourses(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String[] skills
    ) {
        return ResponseEntity.ok(courseService.getCourses(pageable, keyword, status, skills));
    }

    // Get only published courses (with pagination, keyword)
    @GetMapping("/published")
    // @PreAuthorize("true")
    public ResponseEntity<PaginationResponse> getPublishedCourses(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String[] skills
    ) {
        return ResponseEntity.ok(courseService.getPublishedCourses(pageable, keyword, skills));
    }

    @GetMapping("mine")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<PaginationResponse> getCoursesForInstructor(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String[] skills
    ){
        return ResponseEntity.ok(courseService.getCoursesForInstructor(pageable, keyword, status, skills));
    }

    // === Get course by id ===
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<CourseDetailResponse> getCourseById(@PathVariable UUID id) {
        return ResponseEntity.ok(courseService.getById(id));
    }

    // === Get published course by slug ===
    @GetMapping("/slug/{slug}")
    // @PreAuthorize("true")
    public ResponseEntity<CourseDetailResponse> getPublishedCourseBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(courseService.getPublishedBySlug(slug));
    }

    // === Get course info for checkout ===
    @GetMapping("/{id}/checkout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CourseCheckoutResponse> getCourseCheckoutInfo(@PathVariable UUID id) {
        return ResponseEntity.ok(courseService.getCheckoutInfoById(id));
    }

    // === Create new course ===
    @PostMapping
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CourseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courseService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<CourseResponse> updateCourse(
            @PathVariable UUID id,
            @Valid @RequestBody CourseRequest request
    ) {
        return ResponseEntity.ok(courseService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<Void> deleteCourse(@PathVariable UUID id) {
        courseService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    public ResponseEntity<CourseResponse> changeCourseStatus(
            @PathVariable UUID id,
            @RequestParam CourseStatus status
    ) {
        return ResponseEntity.ok(courseService.changeStatus(id, status));
    }
    
    /**
     * Get monthly growth statistics for the current instructor
     * Returns revenue and student count broken down by weekly periods (7-day intervals)
     * Periods: 1-7, 8-14, 15-21, 22-28, 29-end of month
     * 
     * @param year Year (e.g., 2025)
     * @param month Month (1-12)
     */
    @GetMapping("/instructor/growth")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<MonthlyGrowthResponse> getMonthlyGrowth(
            @RequestParam Integer year,
            @RequestParam Integer month
    ) {
        UUID instructorId = SecurityUtil.getCurrentUserId();
        MonthlyGrowthResponse growth = courseService.getMonthlyGrowth(instructorId, year, month);
        return ResponseEntity.ok(growth);
    }
}
