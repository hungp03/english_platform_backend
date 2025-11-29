package com.english.api.assessment.service;

import com.english.api.assessment.dto.request.SubmitAttemptRequest;
import com.english.api.assessment.dto.response.AttemptAnswersResponse;
import com.english.api.assessment.dto.response.AttemptResponse;
import com.english.api.common.dto.PaginationResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AttemptService {
    AttemptAnswersResponse getAttemptAnswers(UUID attemptId);

    AttemptResponse submitOneShot(SubmitAttemptRequest req);

    AttemptResponse getAttempt(UUID attemptId);

    PaginationResponse listAttemptsByUser(Pageable pageable);

    PaginationResponse listAttemptsByUserAndQuiz(UUID quizId, Pageable pageable);

    PaginationResponse listAttemptsByQuiz(UUID quizId, Pageable pageable);
}
