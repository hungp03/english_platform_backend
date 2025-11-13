package com.english.api.quiz.service;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.quiz.dto.request.QuestionCreateRequest;
import com.english.api.quiz.dto.request.QuestionOptionCreateRequest;
import com.english.api.quiz.dto.request.QuestionUpdateRequest;
import com.english.api.quiz.dto.response.QuestionOptionResponse;
import com.english.api.quiz.dto.response.QuestionResponse;
import com.english.api.quiz.model.Question;
import com.english.api.quiz.model.QuestionOption;
import com.english.api.quiz.model.Quiz;
import com.english.api.quiz.repository.QuestionOptionRepository;
import com.english.api.quiz.repository.QuestionRepository;
import com.english.api.quiz.repository.QuizRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

public interface QuestionService {
  public QuestionResponse create(QuestionCreateRequest req);
  public QuestionResponse update(UUID id, QuestionUpdateRequest req);
  public void delete(UUID id);
  public QuestionResponse get(UUID id);
  public PaginationResponse listByQuiz(UUID quizId, Pageable pageable);
  PaginationResponse listBySection(UUID sectionId, Pageable pageable);
}
