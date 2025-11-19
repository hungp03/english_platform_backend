package com.english.api.quiz.service;

import com.english.api.quiz.dto.request.QuizTypeCreateRequest;
import com.english.api.quiz.dto.request.QuizTypeUpdateRequest;
import com.english.api.quiz.dto.response.QuizTypeResponse;

import java.util.List;
import java.util.UUID;


public interface QuizTypeService {
    QuizTypeResponse create(QuizTypeCreateRequest req);

    QuizTypeResponse update(UUID id, QuizTypeUpdateRequest req);

    void delete(UUID id);

    QuizTypeResponse get(UUID id);

    List<QuizTypeResponse> listAll();
}
