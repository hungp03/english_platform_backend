package com.english.api.assessment.controller;

import com.english.api.assessment.dto.request.SubmitAttemptRequest;
import com.english.api.assessment.dto.response.AttemptResponse;
import com.english.api.assessment.service.AttemptService;
import com.english.api.common.dto.PaginationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    public PaginationResponse myAttempts(@RequestParam(value = "page", defaultValue = "1") int page,
                                         @RequestParam(value = "pageSize", defaultValue = "20") int pageSize,
                                         @RequestParam(value = "quizId", required = false) UUID quizId) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), pageSize);
        if (quizId != null) return attemptService.listAttemptsByUserAndQuiz(quizId, pageable);
        return attemptService.listAttemptsByUser(pageable);
    }

    @GetMapping
    public PaginationResponse listByQuiz(@RequestParam("quizId") UUID quizId,
                                         @RequestParam(value = "page", defaultValue = "1") int page,
                                         @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), pageSize);
        return attemptService.listAttemptsByQuiz(quizId, pageable);
    }
}
