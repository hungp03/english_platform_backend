package com.english.api.quiz.controller.admin;

import com.english.api.quiz.dto.request.QuestionOptionCreateRequest;
import com.english.api.quiz.dto.request.QuestionOptionUpdateRequest;
import com.english.api.quiz.dto.response.QuestionOptionResponse;
import com.english.api.quiz.service.QuestionOptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/quiz/question-options")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminQuestionOptionController {

    private final QuestionOptionService service;

    @PostMapping("/{questionId}")
    public ResponseEntity<QuestionOptionResponse> create(@PathVariable UUID questionId, @Valid @RequestBody QuestionOptionCreateRequest req) {
        return ResponseEntity.ok(service.create(questionId, req));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<QuestionOptionResponse> update(@PathVariable UUID id, @Valid @RequestBody QuestionOptionUpdateRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionOptionResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(service.get(id));
    }
}