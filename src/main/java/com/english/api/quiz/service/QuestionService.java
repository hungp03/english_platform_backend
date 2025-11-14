package com.english.api.quiz.service;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.quiz.dto.request.QuestionCreateRequest;
import com.english.api.quiz.dto.request.QuestionUpdateRequest;
import com.english.api.quiz.dto.response.QuestionResponse;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public interface QuestionService {
  public QuestionResponse create(QuestionCreateRequest req);
  public QuestionResponse update(UUID id, QuestionUpdateRequest req);
  public void delete(UUID id);
  public QuestionResponse get(UUID id);
  public PaginationResponse listByQuiz(UUID quizId, Pageable pageable);
  PaginationResponse listBySection(UUID sectionId, Pageable pageable);
}
