package com.english.api.quiz.service.impl;

import com.english.api.common.exception.CannotDeleteException;
import com.english.api.common.exception.ResourceAlreadyExistsException;
import com.english.api.common.exception.ResourceInvalidException;
import com.english.api.common.exception.ResourceNotFoundException;
import com.english.api.quiz.dto.request.QuizTypeCreateRequest;
import com.english.api.quiz.dto.request.QuizTypeUpdateRequest;
import com.english.api.quiz.dto.response.QuizTypeResponse;
import com.english.api.quiz.model.QuizType;
import com.english.api.quiz.repository.QuizRepository;
import com.english.api.quiz.repository.QuizSectionRepository;
import com.english.api.quiz.repository.QuizTypeRepository;
import com.english.api.quiz.service.QuizTypeService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuizTypeServiceImpl implements QuizTypeService {

    private final QuizTypeRepository quizTypeRepository;
    private final QuizRepository quizRepository;
    private final QuizSectionRepository quizSectionRepository;

    @Transactional
    public QuizTypeResponse create(QuizTypeCreateRequest request) {
        if (quizTypeRepository.existsByNameIgnoreCase(request.name().trim())) {
            throw new ResourceAlreadyExistsException("QuizType name already exists");
        }
        QuizType quizType = QuizType.builder()
                .name(request.name().trim())
                .description(request.description())
                .build();
        quizType = quizTypeRepository.save(quizType);
        return toResponse(quizType);
    }

    @Transactional
    public QuizTypeResponse update(UUID id, QuizTypeUpdateRequest request) {
        QuizType quizType = quizTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException( "QuizType not found"));
        if (request.name() != null) {
            if (request.name().trim().isEmpty()) {
                throw new ResourceInvalidException("Quiz type name cannot be empty");
            }
            if (quizTypeRepository.existsByNameIgnoreCase(request.name().trim())) {
                throw new ResourceAlreadyExistsException("QuizType name already exists");
            }
            quizType.setName(request.name().trim());
        }
        if (request.description() != null) quizType.setDescription(request.description());
        return toResponse(quizTypeRepository.save(quizType));
    }

    @Transactional
    public void delete(UUID id) {
        QuizType quizType = quizTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("QuizType not found"));

        if (quizRepository.existsByQuizType_Id(id)) {
            throw new CannotDeleteException("Cannot delete quiz type because there are quizzes using it");
        }

        if (quizSectionRepository.existsByQuizType_Id(id)) {
            throw new CannotDeleteException("Cannot delete quiz type because there are quiz sections using it");
        }

        quizTypeRepository.delete(quizType);
    }

    @Transactional(readOnly = true)
    public QuizTypeResponse get(UUID id) {
        QuizType quizType = quizTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("QuizType not found"));
        return toResponse(quizType);
    }

    @Transactional(readOnly = true)
    public List<QuizTypeResponse> listAll() {
        return quizTypeRepository.findAll().stream().map(this::toResponse).toList();
    }

    private QuizTypeResponse toResponse(QuizType quizType) {
        return new QuizTypeResponse(
                quizType.getId(),
                // quizType.getCode(),
                quizType.getName(),
                quizType.getDescription(),
                quizType.getCreatedAt(),
                quizType.getUpdatedAt()
        );
    }
}