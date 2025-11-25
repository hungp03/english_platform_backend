package com.english.api.assessment.controller;

import com.english.api.assessment.dto.request.SubmitAttemptRequest;
import com.english.api.assessment.dto.response.AttemptAnswersResponse;
import com.english.api.assessment.dto.response.AttemptResponse;
import com.english.api.assessment.service.AttemptService;
import com.english.api.common.dto.PaginationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/assessment/attempts")
public class AttemptController {

    private final AttemptService attemptService;

    @PostMapping("/submit")
    public ResponseEntity<AttemptResponse> submit(@RequestBody SubmitAttemptRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(attemptService.submitOneShot(req));
    }

    @GetMapping("/{attemptId}")
    public ResponseEntity<AttemptResponse> get(@PathVariable UUID attemptId) {
        return ResponseEntity.ok(attemptService.getAttempt(attemptId));
    }

    @GetMapping("/my")
    public ResponseEntity<PaginationResponse> myAttempts(@PageableDefault(size = 20) Pageable pageable,
                                                          @RequestParam(value = "quizId", required = false) UUID quizId) {
        if (quizId != null) {
            return ResponseEntity.ok(attemptService.listAttemptsByUserAndQuiz(quizId, pageable));
        }
        return ResponseEntity.ok(attemptService.listAttemptsByUser(pageable));
    }

    @GetMapping
    public ResponseEntity<PaginationResponse> listByQuiz(@RequestParam("quizId") UUID quizId,
                                                          @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(attemptService.listAttemptsByQuiz(quizId, pageable));
    }

    @GetMapping("/{attemptId}/answers")
    public ResponseEntity<AttemptAnswersResponse> getAnswers(@PathVariable UUID attemptId) {
        return ResponseEntity.ok(attemptService.getAttemptAnswers(attemptId));
    }
}
