package com.english.api.quiz.service;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.quiz.dto.request.QuestionCreateRequest;
import com.english.api.quiz.dto.request.QuestionUpdateRequest;
import com.english.api.quiz.dto.response.QuestionResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface QuestionService {
    QuestionResponse create(QuestionCreateRequest req);

    QuestionResponse update(UUID id, QuestionUpdateRequest req);

    void delete(UUID id);

    QuestionResponse get(UUID id);

    PaginationResponse listByQuiz(UUID quizId, Pageable pageable);

    PaginationResponse listBySection(UUID sectionId, Pageable pageable);
}
