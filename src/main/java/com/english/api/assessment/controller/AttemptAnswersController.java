package com.english.api.assessment.controller;

import com.english.api.assessment.dto.response.AttemptAnswersResponse;
import com.english.api.assessment.service.AttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/assessment/attempts")
public class AttemptAnswersController {

    private final AttemptService attemptService;

    @GetMapping("/{attemptId}/answers")
    public AttemptAnswersResponse getAnswers(@PathVariable UUID attemptId) {
        return attemptService.getAttemptAnswers(attemptId);
    }
}
