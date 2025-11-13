
package com.english.api.quiz.service;
import com.english.api.common.dto.PaginationResponse;
import com.english.api.quiz.dto.request.QuizSectionCreateRequest;
import com.english.api.quiz.dto.request.QuizSectionUpdateRequest;
import com.english.api.quiz.dto.response.QuizSectionResponse;

import java.util.List; import java.util.UUID;

public interface QuizSectionService {
    QuizSectionResponse create(QuizSectionCreateRequest req);
    QuizSectionResponse update(UUID id, QuizSectionUpdateRequest req);
    void delete(UUID id);
    QuizSectionResponse get(UUID id);
    PaginationResponse page(int page, int pageSize);
    PaginationResponse pageByQuizType(UUID quizTypeId, int page, int pageSize);
    List<QuizSectionResponse> listByQuizType(UUID quizTypeId);
}
