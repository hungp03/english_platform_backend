package com.english.api.quiz.service;

import com.english.api.quiz.dto.request.QuestionOptionCreateRequest;
import com.english.api.quiz.dto.request.QuestionOptionUpdateRequest;
import com.english.api.quiz.dto.response.QuestionOptionResponse;

import java.util.UUID;

public interface QuestionOptionService {
    QuestionOptionResponse create(UUID questionId, QuestionOptionCreateRequest req);

    QuestionOptionResponse update(UUID id, QuestionOptionUpdateRequest req);

    void delete(UUID id);

    QuestionOptionResponse get(UUID id);
}
