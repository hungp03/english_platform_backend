package com.english.api.course.controller;

import com.english.api.course.dto.request.LessonBlockRequest;
import com.english.api.course.dto.response.LessonBlockResponse;
import com.english.api.course.service.LessonBlockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Created by hungpham on 10/8/2025
 */
@RestController
@RequestMapping("/api/lessons/{lessonId}/blocks")
@RequiredArgsConstructor
public class LessonBlockController {

    private final LessonBlockService service;

    @GetMapping
    public ResponseEntity<List<LessonBlockResponse>> list(@PathVariable UUID lessonId) {
        return ResponseEntity.ok(service.list(lessonId));
    }

    @GetMapping("/{blockId}")
    public ResponseEntity<LessonBlockResponse> getById(
            @PathVariable UUID lessonId,
            @PathVariable UUID blockId
    ) {
        return ResponseEntity.ok(service.getById(lessonId, blockId));
    }

    @PostMapping
    public ResponseEntity<LessonBlockResponse> create(
            @PathVariable UUID lessonId,
            @Valid @RequestBody LessonBlockRequest request
    ) {
        return ResponseEntity.ok(service.create(lessonId, request));
    }

    @PutMapping("/{blockId}")
    public ResponseEntity<LessonBlockResponse> update(
            @PathVariable UUID lessonId,
            @PathVariable UUID blockId,
            @Valid @RequestBody LessonBlockRequest request
    ) {
        return ResponseEntity.ok(service.update(lessonId, blockId, request));
    }

    @DeleteMapping("/{blockId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID lessonId,
            @PathVariable UUID blockId
    ) {
        service.delete(lessonId, blockId);
        return ResponseEntity.noContent().build();
    }
}

