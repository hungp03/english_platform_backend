package com.english.api.quiz.service;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.quiz.dto.request.QuizCreateRequest;
import com.english.api.quiz.dto.request.QuizUpdateRequest;
import com.english.api.quiz.dto.response.PublicQuizDetailResponse;
import com.english.api.quiz.dto.response.QuizResponse;
import com.english.api.quiz.model.enums.*;

import java.util.UUID;
import org.springframework.data.domain.*;



public interface QuizService {
  public PaginationResponse search(String keyword, UUID quizTypeId, UUID quizSectionId, QuizStatus status, QuizSkill skill, Pageable pageable);
  public QuizResponse get(UUID id);
  public QuizResponse create(QuizCreateRequest r);
  public QuizResponse update(UUID id, QuizUpdateRequest r);
  public void delete(UUID id);
  public PaginationResponse publicQuizBySection(UUID sectionId, Pageable pageable);
  PaginationResponse listPublishedBySection(UUID sectionId, Pageable pageable);
  PaginationResponse publicSearch(UUID quizTypeId, UUID quizSectionId, QuizSkill skill, Pageable pageable);
  public PublicQuizDetailResponse getPublicQuiz(UUID id);
}
