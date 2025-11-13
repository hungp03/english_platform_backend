package com.english.api.quiz.service;

import com.english.api.quiz.dto.request.QuizTypeCreateRequest;
import com.english.api.quiz.dto.request.QuizTypeUpdateRequest;
import com.english.api.quiz.dto.response.QuizTypeResponse;
import java.util.List;
import java.util.UUID;


public interface QuizTypeService {
  public QuizTypeResponse create(QuizTypeCreateRequest req);
  public QuizTypeResponse update(UUID id, QuizTypeUpdateRequest req);
  public void delete(UUID id);
  public QuizTypeResponse get(UUID id);
  public List<QuizTypeResponse> listAll();
}
