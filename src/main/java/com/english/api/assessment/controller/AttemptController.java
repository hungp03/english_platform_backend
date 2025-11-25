package com.english.api.assessment.controller;

import com.english.api.assessment.dto.request.SubmitAttemptRequest;
import com.english.api.assessment.dto.response.AttemptAnswersResponse;
import com.english.api.assessment.dto.response.AttemptResponse;
import com.english.api.assessment.service.AttemptService;
import com.english.api.common.dto.PaginationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/assessment/attempts")
public class AttemptController {

    private final AttemptService attemptService;

    @PostMapping("/submit")
    public AttemptResponse submit(@RequestBody SubmitAttemptRequest req) {
        return attemptService.submitOneShot(req);
    }

    @GetMapping("/{attemptId}")
    public AttemptResponse get(@PathVariable UUID attemptId) {
        return attemptService.getAttempt(attemptId);
    }

    @GetMapping("/my")
    public PaginationResponse myAttempts(@PageableDefault(size = 20) Pageable pageable,
                                         @RequestParam(value = "quizId", required = false) UUID quizId) {
        if (quizId != null) return attemptService.listAttemptsByUserAndQuiz(quizId, pageable);
        return attemptService.listAttemptsByUser(pageable);
    }

    @GetMapping
    public PaginationResponse listByQuiz(@RequestParam("quizId") UUID quizId,
                                         @PageableDefault(size = 20) Pageable pageable) {
        return attemptService.listAttemptsByQuiz(quizId, pageable);
    }

    @GetMapping("/{attemptId}/answers")
    public AttemptAnswersResponse getAnswers(@PathVariable UUID attemptId) {
        return attemptService.getAttemptAnswers(attemptId);
    }
}
