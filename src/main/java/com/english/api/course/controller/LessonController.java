package com.english.api.course.controller;

import com.english.api.course.dto.request.LessonRequest;
import com.english.api.course.dto.response.LessonResponse;
import com.english.api.course.service.LessonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Created by hungpham on 10/7/2025
 */
@RestController
@RequestMapping("/api/modules/{moduleId}/lessons")
@RequiredArgsConstructor
public class LessonController {
    private final LessonService service;

    // --- List ---
    @GetMapping
    public ResponseEntity<List<LessonResponse>> list(@PathVariable UUID moduleId) {
        return ResponseEntity.ok(service.list(moduleId));
    }

    // --- Get by ID ---
    @GetMapping("/{lessonId}")
    public ResponseEntity<LessonResponse> getById(
            @PathVariable UUID moduleId,
            @PathVariable UUID lessonId
    ) {
        return ResponseEntity.ok(service.getById(moduleId, lessonId));
    }

    // --- Create ---
    @PostMapping
    public ResponseEntity<LessonResponse> create(
            @PathVariable UUID moduleId,
            @Valid @RequestBody LessonRequest request
    ) {
        return ResponseEntity.ok(service.create(moduleId, request));
    }

    // --- Update ---
    @PutMapping("/{lessonId}")
    public ResponseEntity<LessonResponse> update(
            @PathVariable UUID moduleId,
            @PathVariable UUID lessonId,
            @Valid @RequestBody LessonRequest request
    ) {
        return ResponseEntity.ok(service.update(moduleId, lessonId, request));
    }

    // --- Delete ---
    @DeleteMapping("/{lessonId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID moduleId,
            @PathVariable UUID lessonId
    ) {
        service.delete(moduleId, lessonId);
        return ResponseEntity.noContent().build();
    }

    // --- Attach Asset ---
    @PostMapping("/{lessonId}/assets/{assetId}")
    public ResponseEntity<LessonResponse> attachAsset(
            @PathVariable UUID moduleId,
            @PathVariable UUID lessonId,
            @PathVariable UUID assetId
    ) {
        return ResponseEntity.ok(service.attachAsset(lessonId, assetId));
    }

    // --- Detach Asset ---
    @DeleteMapping("/{lessonId}/assets/{assetId}")
    public ResponseEntity<LessonResponse> detachAsset(
            @PathVariable UUID moduleId,
            @PathVariable UUID lessonId,
            @PathVariable UUID assetId
    ) {
        return ResponseEntity.ok(service.detachAsset(lessonId, assetId));
    }
}
