package com.english.api.quiz.service;

import com.english.api.quiz.dto.request.QuestionOptionCreateRequest;
import com.english.api.quiz.dto.request.QuestionOptionUpdateRequest;
import com.english.api.quiz.dto.response.QuestionOptionResponse;
import com.english.api.quiz.model.Question;
import com.english.api.quiz.model.QuestionOption;
import com.english.api.quiz.repository.QuestionOptionRepository;
import com.english.api.quiz.repository.QuestionRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

public interface QuestionOptionService {
  public QuestionOptionResponse create(UUID questionId, QuestionOptionCreateRequest req);
  public QuestionOptionResponse update(UUID id, QuestionOptionUpdateRequest req);
  public void delete(UUID id);
  public QuestionOptionResponse get(UUID id);
}
