package com.english.api.quiz.controller.admin;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.quiz.dto.request.QuestionCreateRequest;
import com.english.api.quiz.dto.request.QuestionUpdateRequest;
import com.english.api.quiz.dto.response.QuestionResponse;
import com.english.api.quiz.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/quiz/questions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminQuestionController {

    private final QuestionService service;

    @PostMapping
    public ResponseEntity<QuestionResponse> create(@Valid @RequestBody QuestionCreateRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<QuestionResponse> update(@PathVariable UUID id, @Valid @RequestBody QuestionUpdateRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(service.get(id));
    }

    @GetMapping("/by-quiz/{quizId}")
    public ResponseEntity<PaginationResponse> listByQuiz(@PathVariable UUID quizId, Pageable pageable) {
        return ResponseEntity.ok(service.listByQuiz(quizId, pageable));
    }

    @GetMapping("/by-section/{sectionId}")
    public ResponseEntity<PaginationResponse> listBySection(@PathVariable UUID sectionId, Pageable pageable) {
        return ResponseEntity.ok(service.listBySection(sectionId, pageable));
    }
}
