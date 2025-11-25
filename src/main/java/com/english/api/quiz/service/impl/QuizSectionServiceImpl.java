package com.english.api.quiz.service.impl;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.common.exception.ResourceNotFoundException;
import com.english.api.quiz.dto.request.QuizSectionCreateRequest;
import com.english.api.quiz.dto.request.QuizSectionUpdateRequest;
import com.english.api.quiz.dto.response.QuizSectionResponse;
import com.english.api.quiz.model.QuizSection;
import com.english.api.quiz.repository.QuizSectionRepository;
import com.english.api.quiz.repository.QuizTypeRepository;
import com.english.api.quiz.service.QuizSectionService;
import org.springframework.data.domain.PageRequest;
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
    public QuizSectionResponse create(QuizSectionCreateRequest req) {
        var quizType = typeRepo.findById(req.quizTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("QuizType not found: " + req.quizTypeId()));

        var section = QuizSection.builder()
                .name(req.name())
                .description(req.description())
                .skill(req.skill())
                .quizType(quizType)
                .build();

        section = sectionRepo.save(section);
        return toDto(section);
    }

    @Override
    @Transactional
    public QuizSectionResponse update(UUID id, QuizSectionUpdateRequest req) {
        var section = sectionRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("QuizSection not found: " + id));

        if (req.name() != null) section.setName(req.name());
        if (req.description() != null) section.setDescription(req.description());
        if (req.skill() != null) section.setSkill(req.skill());
        if (req.quizTypeId() != null) {
            var quizType = typeRepo.findById(req.quizTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("QuizType not found: " + req.quizTypeId()));
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
        var section = sectionRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("QuizSection not found: " + id));
        return toDto(section);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse page(int page, int pageSize) {
        var pageable = PageRequest.of(Math.max(0, page - 1), pageSize);
        var p = sectionRepo.findAll(pageable).map(this::toDto);
        return PaginationResponse.from(p, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse pageByQuizType(UUID quizTypeId, int page, int pageSize) {
        var pageable = PageRequest.of(Math.max(0, page - 1), pageSize);
        var p = sectionRepo.findByQuizTypeId(quizTypeId, pageable).map(this::toDto);
        return PaginationResponse.from(p, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuizSectionResponse> listByQuizType(UUID quizTypeId) {
        return sectionRepo.findByQuizTypeId(quizTypeId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private QuizSectionResponse toDto(QuizSection s) {
        return new QuizSectionResponse(
                s.getId(),
                s.getName(),
                s.getDescription(),
                s.getSkill() != null ? s.getSkill().name() : null,
                s.getQuizType() != null ? s.getQuizType().getId() : null,
                s.getQuizType() != null ? s.getQuizType().getName() : null,
                s.getCreatedAt() != null ? ISO.format(s.getCreatedAt()) : null,
                s.getUpdatedAt() != null ? ISO.format(s.getUpdatedAt()) : null
        );
    }
}
