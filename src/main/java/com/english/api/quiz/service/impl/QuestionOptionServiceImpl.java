package com.english.api.quiz.service.impl;

import com.english.api.quiz.dto.request.QuestionOptionCreateRequest;
import com.english.api.quiz.dto.request.QuestionOptionUpdateRequest;
import com.english.api.quiz.dto.response.QuestionOptionResponse;
import com.english.api.quiz.model.Question;
import com.english.api.quiz.model.QuestionOption;
import com.english.api.quiz.repository.QuestionOptionRepository;
import com.english.api.quiz.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuestionOptionServiceImpl implements com.english.api.quiz.service.QuestionOptionService {

    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository optionRepository;

    @Transactional
    public QuestionOptionResponse create(UUID questionId, QuestionOptionCreateRequest req) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Question not found"));
        QuestionOption op = QuestionOption.builder()
                .question(question)
                .content(req.content())
                .correct(Boolean.TRUE.equals(req.correct()))
                .explanation(req.explanation())
                .orderIndex(req.orderIndex() == null ? 0 : req.orderIndex())
                .build();
        op = optionRepository.save(op);
        return toResponse(op);
    }

    @Transactional
    public QuestionOptionResponse update(UUID id, QuestionOptionUpdateRequest req) {
        QuestionOption op = optionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "QuestionOption not found"));
        if (req.content() != null) op.setContent(req.content());
        if (req.correct() != null) op.setCorrect(req.correct());
        if (req.explanation() != null) op.setExplanation(req.explanation());
        if (req.orderIndex() != null) op.setOrderIndex(req.orderIndex());
        op = optionRepository.save(op);
        return toResponse(op);
    }

    @Transactional
    public void delete(UUID id) {
        QuestionOption op = optionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "QuestionOption not found"));
        optionRepository.delete(op);
    }

    @Transactional(readOnly = true)
    public QuestionOptionResponse get(UUID id) {
        QuestionOption op = optionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "QuestionOption not found"));
        return toResponse(op);
    }

    private QuestionOptionResponse toResponse(QuestionOption e) {
        return new QuestionOptionResponse(e.getId(), e.getContent(), e.isCorrect(), e.getExplanation(), e.getOrderIndex());
    }
}