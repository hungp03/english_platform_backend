package com.english.api.quiz.service.impl;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.quiz.dto.request.QuestionCreateRequest;
import com.english.api.quiz.dto.request.QuestionOptionCreateRequest;
import com.english.api.quiz.dto.request.QuestionUpdateRequest;
import com.english.api.quiz.dto.response.QuestionOptionResponse;
import com.english.api.quiz.dto.response.QuestionResponse;
import com.english.api.quiz.model.Question;
import com.english.api.quiz.model.QuestionOption;
import com.english.api.quiz.model.Quiz;
import com.english.api.quiz.repository.QuestionRepository;
import com.english.api.quiz.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements com.english.api.quiz.service.QuestionService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;

    @Transactional
    public QuestionResponse create(QuestionCreateRequest req) {
        Quiz quiz = quizRepository.findById(req.quizId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quiz not found"));
        Question q = Question.builder()
                .quiz(quiz)
                .content(req.content())
                .orderIndex(req.orderIndex())
                .build();

        if (req.options() != null && !req.options().isEmpty()) {
            List<QuestionOption> opts = new ArrayList<>();
            for (QuestionOptionCreateRequest o : req.options()) {
                QuestionOption op = QuestionOption.builder()
                        .question(q)
                        .content(o.content())
                        .correct(Boolean.TRUE.equals(o.correct()))
                        .explanation(o.explanation())
                        .orderIndex(o.orderIndex() == null ? 0 : o.orderIndex())
                        .build();
                opts.add(op);
            }
            q.setOptions(new java.util.LinkedHashSet<>(opts));
        }

        q = questionRepository.save(q);
        return toResponse(q);
    }

    @Transactional
    public QuestionResponse update(UUID id, QuestionUpdateRequest req) {
        Question q = questionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found"));

        if (req.quizId() != null) {
            Quiz quiz = quizRepository.findById(req.quizId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quiz not found"));
            q.setQuiz(quiz);
        }
        if (req.content() != null) q.setContent(req.content());
        if (req.orderIndex() != null) q.setOrderIndex(req.orderIndex());

        if (req.options() != null) {
            if (q.getOptions() == null) {
                q.setOptions(new java.util.LinkedHashSet<>());
            }
            q.getOptions().clear();
            for (QuestionOptionCreateRequest o : req.options()) {
                QuestionOption op = QuestionOption.builder()
                        .question(q)
                        .content(o.content())
                        .correct(Boolean.TRUE.equals(o.correct()))
                        .explanation(o.explanation())
                        .orderIndex(o.orderIndex() == null ? 0 : o.orderIndex())
                        .build();
                q.getOptions().add(op);
            }
        }

        q = questionRepository.save(q);
        return toResponse(q);
    }

    @Transactional
    public void delete(UUID id) {
        Question q = questionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found"));
        questionRepository.delete(q);
    }

    @Transactional(readOnly = true)
    public QuestionResponse get(UUID id) {
        Question q = questionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found"));
        return toResponse(q);
    }

    @Transactional(readOnly = true)
    public PaginationResponse listByQuiz(UUID quizId, Pageable pageable) {
        Page<Question> page = questionRepository.findByQuiz_IdOrderByOrderIndexAsc(quizId, pageable);
        return PaginationResponse.from(page.map(this::toResponse), pageable);
    }

    private QuestionResponse toResponse(Question e) {
        List<QuestionOptionResponse> options = e.getOptions() != null 
                ? e.getOptions().stream()
                    .map(op -> new QuestionOptionResponse(op.getId(), op.getContent(), op.isCorrect(), op.getExplanation(), op.getOrderIndex()))
                    .collect(java.util.stream.Collectors.toList())
                : new ArrayList<>();
        return new QuestionResponse(
                e.getId(),
                e.getQuiz().getId(),
                e.getContent(),
                e.getOrderIndex(),
                options,
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }


    @Override
    @Transactional(readOnly = true)
    public PaginationResponse listBySection(UUID sectionId, Pageable pageable) {
        Page<Question> page = questionRepository.findByQuiz_QuizSection_Id(sectionId, pageable);
        return PaginationResponse.from(page.map(this::toResponse), pageable);
    }
}
