package com.english.api.quiz.service.impl;

import com.english.api.common.exception.ResourceAlreadyExistsException;
import com.english.api.quiz.dto.request.QuizTypeCreateRequest;
import com.english.api.quiz.dto.request.QuizTypeUpdateRequest;
import com.english.api.quiz.dto.response.QuizTypeResponse;
import com.english.api.quiz.model.QuizType;
import com.english.api.quiz.repository.QuizTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuizTypeServiceImpl implements com.english.api.quiz.service.QuizTypeService {

    private final QuizTypeRepository quizTypeRepository;

    @Transactional
    public QuizTypeResponse create(QuizTypeCreateRequest req) {
        if (quizTypeRepository.existsByNameIgnoreCase(req.name().trim())) {
            throw new ResourceAlreadyExistsException("QuizType name already exists");
        }
        QuizType entity = QuizType.builder()
                .name(req.name().trim())
                .description(req.description())
                .build();
        entity = quizTypeRepository.save(entity);
        return toResponse(entity);
    }

    @Transactional
    public QuizTypeResponse update(UUID id, QuizTypeUpdateRequest req) {
        QuizType entity = quizTypeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "QuizType not found"));
        if (req.name() != null) {
            if (req.name().trim().isEmpty()) {
                throw new IllegalArgumentException("Quiz type name cannot be empty");
            }
            if (quizTypeRepository.existsByNameIgnoreCase(req.name().trim())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "QuizType name already exists");
            }
            entity.setName(req.name().trim());
        }
        if (req.description() != null) entity.setDescription(req.description());
        return toResponse(quizTypeRepository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        QuizType entity = quizTypeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "QuizType not found"));
        quizTypeRepository.delete(entity);
    }

    @Transactional(readOnly = true)
    public QuizTypeResponse get(UUID id) {
        QuizType entity = quizTypeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "QuizType not found"));
        return toResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<QuizTypeResponse> listAll() {
        return quizTypeRepository.findAll().stream().map(this::toResponse).toList();
    }

    private QuizTypeResponse toResponse(QuizType e) {
        return new QuizTypeResponse(
                e.getId(),
                // e.getCode(),
                e.getName(),
                e.getDescription(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}