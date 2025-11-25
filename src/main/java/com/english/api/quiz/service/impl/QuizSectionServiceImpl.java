package com.english.api.quiz.service.impl;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.common.exception.ResourceNotFoundException;
import com.english.api.quiz.dto.request.QuizSectionCreateRequest;
import com.english.api.quiz.dto.request.QuizSectionUpdateRequest;
import com.english.api.quiz.dto.response.QuizSectionResponse;
import com.english.api.quiz.model.QuizSection;
import com.english.api.quiz.model.QuizType;
import com.english.api.quiz.repository.QuizSectionRepository;
import com.english.api.quiz.repository.QuizTypeRepository;
import com.english.api.quiz.service.QuizSectionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class QuizSectionServiceImpl implements QuizSectionService {

    private final QuizSectionRepository sectionRepo;
    private final QuizTypeRepository typeRepo;
    private final DateTimeFormatter ISO = DateTimeFormatter.ISO_INSTANT;

    public QuizSectionServiceImpl(QuizSectionRepository sectionRepo, QuizTypeRepository typeRepo) {
        this.sectionRepo = sectionRepo;
        this.typeRepo = typeRepo;
    }

    @Override
    @Transactional
    public QuizSectionResponse create(QuizSectionCreateRequest request) {
        QuizType quizType = typeRepo.findById(request.quizTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("QuizType not found: " + request.quizTypeId()));

        QuizSection section = QuizSection.builder()
                .name(request.name())
                .description(request.description())
                .skill(request.skill())
                .quizType(quizType)
                .build();

        section = sectionRepo.save(section);
        return toDto(section);
    }

    @Override
    @Transactional
    public QuizSectionResponse update(UUID id, QuizSectionUpdateRequest request) {
        QuizSection section = sectionRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("QuizSection not found: " + id));

        if (request.name() != null) section.setName(request.name());
        if (request.description() != null) section.setDescription(request.description());
        if (request.skill() != null) section.setSkill(request.skill());
        if (request.quizTypeId() != null) {
            QuizType quizType = typeRepo.findById(request.quizTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("QuizType not found: " + request.quizTypeId()));
            section.setQuizType(quizType);
        }

        section = sectionRepo.save(section);
        return toDto(section);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        sectionRepo.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public QuizSectionResponse get(UUID id) {
        QuizSection section = sectionRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("QuizSection not found: " + id));
        return toDto(section);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse page(int page, int pageSize) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), pageSize);
        Page<QuizSectionResponse> pageResponse = sectionRepo.findAll(pageable).map(this::toDto);
        return PaginationResponse.from(pageResponse, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse pageByQuizType(UUID quizTypeId, int page, int pageSize) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), pageSize);
        Page<QuizSectionResponse> pageResponse = sectionRepo.findByQuizTypeId(quizTypeId, pageable).map(this::toDto);
        return PaginationResponse.from(pageResponse, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizSectionResponse> listByQuizType(UUID quizTypeId) {
        return sectionRepo.findByQuizTypeId(quizTypeId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private QuizSectionResponse toDto(QuizSection section) {
        return new QuizSectionResponse(
                section.getId(),
                section.getName(),
                section.getDescription(),
                section.getSkill() != null ? section.getSkill().name() : null,
                section.getQuizType() != null ? section.getQuizType().getId() : null,
                section.getQuizType() != null ? section.getQuizType().getName() : null,
                section.getCreatedAt() != null ? ISO.format(section.getCreatedAt()) : null,
                section.getUpdatedAt() != null ? ISO.format(section.getUpdatedAt()) : null
        );
    }
}
