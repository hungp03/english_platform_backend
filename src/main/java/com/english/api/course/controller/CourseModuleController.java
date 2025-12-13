package com.english.api.course.controller;

import com.english.api.course.dto.request.CourseModuleRequest;
import com.english.api.course.dto.request.CourseModuleUpdateRequest;
import com.english.api.course.dto.response.CourseModuleResponse;
import com.english.api.course.dto.response.CourseModuleUpdateResponse;
import com.english.api.course.service.CourseModuleService;
import com.english.api.user.annotation.ActiveInstructor;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Created by hungpham on 10/4/2025
 */
@RestController
@RequestMapping("/api/courses/{courseId}/modules")
@RequiredArgsConstructor
public class CourseModuleController {
    private final CourseModuleService service;

    @GetMapping
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<List<CourseModuleResponse>> list(@PathVariable UUID courseId) {
        return ResponseEntity.ok(service.list(courseId));
    }

    @GetMapping("/published")
    // Public route
    public ResponseEntity<List<CourseModuleResponse>> listPublished(@PathVariable UUID courseId) {
        return ResponseEntity.ok(service.listPublished(courseId));
    }

    @GetMapping("/{moduleId}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<CourseModuleResponse> getById(
            @PathVariable UUID courseId,
            @PathVariable UUID moduleId
    ) {
        return ResponseEntity.ok(service.getById(courseId, moduleId));
    }

    @PostMapping
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @ActiveInstructor
    public ResponseEntity<CourseModuleResponse> create(
            @PathVariable UUID courseId,
            @Valid @RequestBody CourseModuleRequest requests
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(courseId, requests));
    }


    @PutMapping
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<CourseModuleUpdateResponse> update(
            @PathVariable UUID courseId,
            @Valid @RequestBody CourseModuleUpdateRequest requests
    ) {
        return ResponseEntity.ok(service.update(courseId, requests));
    }


    @DeleteMapping("/{moduleId}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<Void> delete(
            @PathVariable UUID courseId,
            @PathVariable UUID moduleId
    ) {
        service.delete(courseId, moduleId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{moduleId}/publish")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    public ResponseEntity<CourseModuleResponse> publish(
            @PathVariable UUID courseId,
            @PathVariable UUID moduleId,
            @RequestParam(name = "publish") Boolean publish
    ) {
        return ResponseEntity.ok(service.publish(courseId, moduleId, publish));
    }
}

