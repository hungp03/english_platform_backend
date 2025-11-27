package com.english.api.quiz.service.impl;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.common.exception.ResourceAlreadyExistsException;
import com.english.api.common.exception.ResourceNotFoundException;
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
import com.english.api.quiz.service.QuestionService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;

    @Transactional
    public QuestionResponse create(QuestionCreateRequest request) {
        Quiz quiz = quizRepository.findById(request.quizId())
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

        if (questionRepository.existsByQuiz_IdAndOrderIndex(request.quizId(), request.orderIndex())) {
            throw new ResourceAlreadyExistsException(
                    "Question with orderIndex " + request.orderIndex() + " already exists in this quiz");
        }

        Question question = Question.builder()
                .quiz(quiz)
                .content(request.content())
                .orderIndex(request.orderIndex())
                .explanation(request.explanation())
                .build();

        if (request.options() != null && !request.options().isEmpty()) {
            List<QuestionOption> options = new ArrayList<>();
            for (QuestionOptionCreateRequest optionRequest : request.options()) {
                QuestionOption option = QuestionOption.builder()
                        .question(question)
                        .content(optionRequest.content())
                        .correct(Boolean.TRUE.equals(optionRequest.correct()))
                        .orderIndex(optionRequest.orderIndex() == null ? 1 : optionRequest.orderIndex())
                        .build();
                options.add(option);
            }
            question.setOptions(new java.util.LinkedHashSet<>(options));
        }

        question = questionRepository.save(question);
        return toResponse(question);
    }

    @Transactional
    public QuestionResponse update(UUID id, QuestionUpdateRequest request) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

        if (request.quizId() != null) {
            Quiz quiz = quizRepository.findById(request.quizId())
                    .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));
            question.setQuiz(quiz);
        }
        if (request.content() != null)
            question.setContent(request.content());
        if (request.explanation() != null)
            question.setExplanation(request.explanation());
        if (request.orderIndex() != null) {
            UUID quizId = request.quizId() != null ? request.quizId() : question.getQuiz().getId();
            if (!request.orderIndex().equals(question.getOrderIndex()) &&
                    questionRepository.existsByQuiz_IdAndOrderIndex(quizId, request.orderIndex())) {
                throw new ResourceAlreadyExistsException(
                        "Question with orderIndex " + request.orderIndex() + " already exists in this quiz");
            }
            question.setOrderIndex(request.orderIndex());
        }

        if (request.options() != null) {
            if (question.getOptions() == null) {
                question.setOptions(new java.util.LinkedHashSet<>());
            }
            question.getOptions().clear();
            for (QuestionOptionCreateRequest optionRequest : request.options()) {
                QuestionOption option = QuestionOption.builder()
                        .question(question)
                        .content(optionRequest.content())
                        .correct(Boolean.TRUE.equals(optionRequest.correct()))
                        .orderIndex(optionRequest.orderIndex() == null ? 1 : optionRequest.orderIndex())
                        .build();
                question.getOptions().add(option);
            }
        }

        question = questionRepository.save(question);
        return toResponse(question);
    }

    @Transactional
    public void delete(UUID id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));
        questionRepository.delete(question);
    }

    @Transactional(readOnly = true)
    public QuestionResponse get(UUID id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));
        return toResponse(question);
    }

    @Transactional(readOnly = true)
    public PaginationResponse listByQuiz(UUID quizId, Pageable pageable) {
        Page<Question> page = questionRepository.findByQuiz_IdOrderByOrderIndexAsc(quizId, pageable);
        return PaginationResponse.from(page.map(this::toResponse), pageable);
    }

    private QuestionResponse toResponse(Question question) {
        List<QuestionOptionResponse> options = question.getOptions() != null
                ? question.getOptions().stream()
                        .map(option -> new QuestionOptionResponse(option.getId(), option.getContent(), option.isCorrect(),
                                option.getOrderIndex()))
                        .collect(java.util.stream.Collectors.toList())
                : new ArrayList<>();
        return new QuestionResponse(
                question.getId(),
                question.getQuiz().getId(),
                question.getContent(),
                question.getOrderIndex(),
                question.getExplanation(),
                options,
                e.getExplanation(),
                e.getCreatedAt(),
                e.getUpdatedAt());
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse listBySection(UUID sectionId, Pageable pageable) {
        Page<Question> page = questionRepository.findByQuiz_QuizSection_Id(sectionId, pageable);
        return PaginationResponse.from(page.map(this::toResponse), pageable);
    }
}
