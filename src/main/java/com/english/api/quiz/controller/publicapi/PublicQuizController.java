package com.english.api.quiz.controller.publicapi;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.quiz.dto.response.PublicQuizDetailResponse;
import com.english.api.quiz.model.enums.QuizSkill;
import com.english.api.quiz.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/quiz")
public class PublicQuizController {

    private final QuizService quizService;

    // Lấy quizzes theo section, mặc định chỉ PUBLISHED
    @GetMapping("/sections/{sectionId}/quizzes")
    public PaginationResponse listPublishedBySection(
            @PathVariable UUID sectionId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize
    ) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), pageSize,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return quizService.listPublishedBySection(sectionId, pageable);
    }

    // Tìm kiếm công khai (PUBLISHED) theo type/section/skill
    @GetMapping("/quizzes")
    public PaginationResponse searchPublished(
            @RequestParam(required = false) UUID quizTypeId,
            @RequestParam(required = false) UUID quizSectionId,
            @RequestParam(required = false) QuizSkill skill,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize
    ) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), pageSize,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return quizService.publicSearch(quizTypeId, quizSectionId, skill, pageable);
    }

    @GetMapping("/quizzes/{id}")
    public ResponseEntity<PublicQuizDetailResponse> getPublicQuiz(@PathVariable UUID id) {
        return ResponseEntity.ok(quizService.getPublicQuiz(id));
}

}
