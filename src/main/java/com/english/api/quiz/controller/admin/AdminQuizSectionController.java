package com.english.api.quiz.controller.admin;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.quiz.dto.request.QuizSectionCreateRequest;
import com.english.api.quiz.dto.request.QuizSectionUpdateRequest;
import com.english.api.quiz.dto.response.QuizSectionResponse;
import com.english.api.quiz.service.QuizSectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/quiz/sections")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminQuizSectionController {

    private final QuizSectionService service;

    @PostMapping
    public QuizSectionResponse create(@RequestBody QuizSectionCreateRequest req) {
        return service.create(req);
    }

    @PutMapping("/{id}")
    public QuizSectionResponse update(@PathVariable UUID id, @RequestBody QuizSectionUpdateRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }

    @GetMapping
    public PaginationResponse list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return service.page(page, pageSize);
    }

    @GetMapping("/by-type/{quizTypeId}")
    public PaginationResponse listByType(
            @PathVariable UUID quizTypeId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {
        return service.pageByQuizType(quizTypeId, page, pageSize);
    }
}
