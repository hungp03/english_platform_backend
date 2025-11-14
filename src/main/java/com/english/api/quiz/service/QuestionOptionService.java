package com.english.api.quiz.service;

import com.english.api.quiz.dto.request.QuestionOptionCreateRequest;
import com.english.api.quiz.dto.request.QuestionOptionUpdateRequest;
import com.english.api.quiz.dto.response.QuestionOptionResponse;
import java.util.UUID;

public interface QuestionOptionService {
  public QuestionOptionResponse create(UUID questionId, QuestionOptionCreateRequest req);
  public QuestionOptionResponse update(UUID id, QuestionOptionUpdateRequest req);
  public void delete(UUID id);
  public QuestionOptionResponse get(UUID id);
}
