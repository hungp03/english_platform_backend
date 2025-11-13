package com.english.api.quiz.controller.publicapi;

import com.english.api.quiz.dto.response.QuizTypeResponse;
import com.english.api.quiz.service.QuizTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/quiz/types")
public class PublicQuizTypeController {

    private final QuizTypeService quizTypeService;

    @GetMapping
    public ResponseEntity<List<QuizTypeResponse>> listAll() {
        return ResponseEntity.ok(quizTypeService.listAll());
    }
}
