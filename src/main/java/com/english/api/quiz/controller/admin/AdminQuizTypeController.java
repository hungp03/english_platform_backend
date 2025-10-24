package com.english.api.quiz.controller.admin;

import com.english.api.quiz.dto.request.QuizTypeCreateRequest;
import com.english.api.quiz.dto.request.QuizTypeUpdateRequest;
import com.english.api.quiz.dto.response.QuizTypeResponse;
import com.english.api.quiz.service.QuizTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/quiz/types")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
public class AdminQuizTypeController {

    private final QuizTypeService service;

    @PostMapping
    public ResponseEntity<QuizTypeResponse> create(@Valid @RequestBody QuizTypeCreateRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<QuizTypeResponse> update(@PathVariable UUID id, @Valid @RequestBody QuizTypeUpdateRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuizTypeResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(service.get(id));
    }

    @GetMapping
    public ResponseEntity<List<QuizTypeResponse>> listAll() {
        return ResponseEntity.ok(service.listAll());
    }
}