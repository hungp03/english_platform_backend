package com.english.api.quiz.controller.admin;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.quiz.dto.request.QuizCreateRequest;
import com.english.api.quiz.dto.request.QuizUpdateRequest;
import com.english.api.quiz.dto.response.QuizResponse;
import com.english.api.quiz.model.enums.*;
// import com.english.api.quiz.model.QuizSkill;
import com.english.api.quiz.service.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/quiz/quizzes")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
public class AdminQuizController {

    private final QuizService service;

    @PostMapping
    public ResponseEntity<QuizResponse> create(@Valid @RequestBody QuizCreateRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<QuizResponse> update(@PathVariable UUID id, @Valid @RequestBody QuizUpdateRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuizResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(service.get(id));
    }

    @GetMapping
    public ResponseEntity<PaginationResponse> search(
            Pageable pageable,
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false) UUID quizTypeId,
            @RequestParam(required = false) UUID quizSectionId,
            @RequestParam(required = false) QuizStatus status,
            @RequestParam(required = false) QuizSkill skill
    ) {
        return ResponseEntity.ok(service.search(
                (keyword == null || keyword.isBlank()) ? null : keyword,
                quizTypeId,
                quizSectionId,
                status,
                skill,
                pageable
        ));
    }
}