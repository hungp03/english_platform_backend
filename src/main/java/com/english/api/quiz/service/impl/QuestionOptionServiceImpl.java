package com.english.api.quiz.service.impl;

import com.english.api.common.exception.ResourceNotFoundException;
import com.english.api.quiz.dto.request.QuestionOptionCreateRequest;
import com.english.api.quiz.dto.request.QuestionOptionUpdateRequest;
import com.english.api.quiz.dto.response.QuestionOptionResponse;
import com.english.api.quiz.model.Question;
import com.english.api.quiz.model.QuestionOption;
import com.english.api.quiz.repository.QuestionOptionRepository;
import com.english.api.quiz.repository.QuestionRepository;
import com.english.api.quiz.service.QuestionOptionService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuestionOptionServiceImpl implements QuestionOptionService {

    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository optionRepository;

    @Transactional
    public QuestionOptionResponse create(UUID questionId, QuestionOptionCreateRequest request) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));
        QuestionOption option = QuestionOption.builder()
                .question(question)
                .content(request.content())
                .correct(Boolean.TRUE.equals(request.correct()))
                .explanation(request.explanation())
                .orderIndex(request.orderIndex() == null ? 0 : request.orderIndex())
                .build();
        option = optionRepository.save(option);
        return toResponse(option);
    }

    @Transactional
    public QuestionOptionResponse update(UUID id, QuestionOptionUpdateRequest request) {
        QuestionOption option = optionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("QuestionOption not found"));
        if (request.content() != null) option.setContent(request.content());
        if (request.correct() != null) option.setCorrect(request.correct());
        if (request.explanation() != null) option.setExplanation(request.explanation());
        if (request.orderIndex() != null) option.setOrderIndex(request.orderIndex());
        option = optionRepository.save(option);
        return toResponse(option);
    }

    @Transactional
    public void delete(UUID id) {
        QuestionOption option = optionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("QuestionOption not found"));
        optionRepository.delete(option);
    }

    @Transactional(readOnly = true)
    public QuestionOptionResponse get(UUID id) {
        QuestionOption option = optionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("QuestionOption not found"));
        return toResponse(option);
    }

    private QuestionOptionResponse toResponse(QuestionOption option) {
        return new QuestionOptionResponse(option.getId(), option.getContent(), option.isCorrect(), option.getExplanation(), option.getOrderIndex());
    }
}