package com.english.api.quiz.service;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.quiz.dto.request.QuizCreateRequest;
import com.english.api.quiz.dto.request.QuizUpdateRequest;
import com.english.api.quiz.dto.response.PublicQuizDetailResponse;
import com.english.api.quiz.dto.response.QuizResponse;
import com.english.api.quiz.model.enums.QuizSkill;
import com.english.api.quiz.model.enums.QuizStatus;
import org.springframework.data.domain.Pageable;

import java.util.UUID;


public interface QuizService {
    PaginationResponse search(String keyword, UUID quizTypeId, UUID quizSectionId, QuizStatus status, QuizSkill skill, Pageable pageable);

    QuizResponse get(UUID id);

    QuizResponse create(QuizCreateRequest r);

    QuizResponse update(UUID id, QuizUpdateRequest r);

    void delete(UUID id);

    PaginationResponse publicQuizBySection(UUID sectionId, Pageable pageable);

    PaginationResponse listPublishedBySection(UUID sectionId, Pageable pageable);

    PaginationResponse publicSearch(UUID quizTypeId, UUID quizSectionId, QuizSkill skill, Pageable pageable);

    PublicQuizDetailResponse getPublicQuiz(UUID id);
}
