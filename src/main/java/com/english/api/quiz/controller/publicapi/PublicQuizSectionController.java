package com.english.api.quiz.controller.publicapi;

import com.english.api.quiz.dto.response.QuizSectionResponse;
import com.english.api.quiz.model.enums.QuizSkill;
import com.english.api.quiz.service.QuizSectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/quiz/types/{quizTypeId}/sections")
public class PublicQuizSectionController {

    private final QuizSectionService sectionService;

    @GetMapping
    public ResponseEntity<List<QuizSectionResponse>> listByType(
            @PathVariable UUID quizTypeId,
            @RequestParam(required = false) QuizSkill skill
    ) {
        List<QuizSectionResponse> sections = sectionService.listByQuizType(quizTypeId);
        if (skill != null) {
            sections = sections.stream()
                    .filter(s -> skill.name().equalsIgnoreCase(s.skill()))
                    .collect(Collectors.toList());
        }
        return ResponseEntity.ok(sections);
    }
}
