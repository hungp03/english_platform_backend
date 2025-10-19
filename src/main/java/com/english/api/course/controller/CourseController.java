package com.english.api.course.controller;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.course.dto.request.CourseRequest;
import com.english.api.course.dto.response.CourseDetailResponse;
import com.english.api.course.dto.response.CourseResponse;
import com.english.api.course.dto.response.CourseWithStatsResponse;
import com.english.api.course.service.CourseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    // Get all courses (with pagination, keyword, publish filter)
    @GetMapping
    public ResponseEntity<PaginationResponse> getCourses(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isPublished,
            @RequestParam(required = false) String[] skills
    ) {
        return ResponseEntity.ok(courseService.getCourses(pageable, keyword, isPublished, skills));
    }

    // Get only published courses (with pagination, keyword)
    @GetMapping("/published")
    public ResponseEntity<PaginationResponse> getPublishedCourses(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String[] skills
    ) {
        return ResponseEntity.ok(courseService.getPublishedCourses(pageable, keyword, skills));
    }

    @GetMapping("mine")
    public ResponseEntity<PaginationResponse> getMineCourses(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isPublished,
            @RequestParam(required = false) String[] skills
    ){
        return ResponseEntity.ok(courseService.getCoursesForInstructor(pageable, keyword, isPublished, skills));
    }

    // === Get course by id ===
    @GetMapping("/{id}")
    public ResponseEntity<CourseDetailResponse> getCourseById(@PathVariable UUID id) {
        return ResponseEntity.ok(courseService.getById(id));
    }

    // === Get published course by slug ===
    @GetMapping("/slug/{slug}")
    public ResponseEntity<CourseDetailResponse> getPublishedCourseBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(courseService.getPublishedBySlug(slug));
    }

    // === Create new course ===
    @PostMapping
    public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CourseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courseService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseResponse> updateCourse(
            @PathVariable UUID id,
            @Valid @RequestBody CourseRequest request
    ) {
        return ResponseEntity.ok(courseService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable UUID id) {
        courseService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/publish")
    public ResponseEntity<CourseResponse> publishCourse(
            @PathVariable UUID id,
            @RequestParam(name = "publish") Boolean publish
    ) {
        return ResponseEntity.ok(courseService.publish(id, publish));
    }
}
