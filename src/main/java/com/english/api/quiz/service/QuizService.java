package com.english.api.quiz.service;
import com.english.api.common.dto.PaginationResponse;
// import com.english.api.common.PaginationResponse;
// import com.english.api.quiz.dto.*;
import com.english.api.quiz.dto.request.QuizCreateRequest;
import com.english.api.quiz.dto.request.QuizUpdateRequest;
import com.english.api.quiz.dto.response.QuizResponse;
import com.english.api.quiz.model.*;
import com.english.api.quiz.repository.*;
import com.english.api.quiz.enums.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

// import static com.english.api.quiz.repository.QuizSpecifications.*;

@Service
@RequiredArgsConstructor
public class QuizService {
    private final QuizRepository quizRepository;
    private final QuizTypeRepository quizTypeRepo; // giả định đã có

    @Transactional(readOnly = true)
    public PaginationResponse search(String keyword, UUID quizTypeId, QuizStatus status, QuizSkill skill, Pageable pageable) {
        Specification<Quiz> spec = (root, query, cb) -> {
            root.fetch("quizType", JoinType.LEFT);
            query.distinct(true);
            return null;
        };
        if (keyword != null && !keyword.isBlank()) {
            Specification<Quiz> s = (root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase().trim() + "%"),
                    cb.like(cb.lower(root.get("description")), "%" + keyword.toLowerCase().trim() + "%")
            );
            spec = spec.and(s);
        }
        if (quizTypeId != null) {
            Specification<Quiz> s = (root, query, cb) -> cb.equal(root.get("quizType").get("id"), quizTypeId);
            spec = spec.and(s);
        }
        if (status != null) {
            Specification<Quiz> s = (root, query, cb) -> cb.equal(root.get("status"), status);
            spec = spec.and(s);
        }
        if (skill != null) {
            Specification<Quiz> s = (root, query, cb) -> cb.equal(root.get("skill"), skill);
            spec = spec.and(s);
        }
        Page<Quiz> page = quizRepository.findAll(spec, pageable);
        return PaginationResponse.from(page.map(this::toResponse), pageable);
    }

    public QuizResponse get(UUID id) {
        return toResponse(findById(id));
    }

    public QuizResponse create(QuizCreateRequest r) {
        QuizType type = quizTypeRepo.findById(r.quizTypeId())
                .orElseThrow(() -> new EntityNotFoundException("QuizType not found"));
        Quiz q = new Quiz();
        q.setTitle(r.title());
        q.setDescription(r.description());
        q.setSkill(r.skill());
        q.setStatus(r.status() == null ? QuizStatus.DRAFT : r.status());
        q.setQuizType(type);
        q.setContextText(r.contextText());
        q.setQuestionText(r.questionText());
        q.setExplanation(r.explanation());
        quizRepository.save(q);
        return toResponse(q);
    }

    public QuizResponse update(UUID id, QuizUpdateRequest r) {
        Quiz q = findById(id);
        if (r.title() != null) q.setTitle(r.title());
        if (r.description() != null) q.setDescription(r.description());
        if (r.skill() != null) q.setSkill(r.skill());
        if (r.status() != null) q.setStatus(r.status());
        if (r.quizTypeId() != null) {
            QuizType type = quizTypeRepo.findById(r.quizTypeId())
                    .orElseThrow(() -> new EntityNotFoundException("QuizType not found"));
            q.setQuizType(type);
        }
        if (r.contextText() != null) q.setContextText(r.contextText());
        if (r.questionText() != null) q.setQuestionText(r.questionText());
        if (r.explanation() != null) q.setExplanation(r.explanation());
        quizRepository.save(q);
        return toResponse(q);
    }

    public void delete(UUID id) {
        quizRepository.deleteById(id);
    }

    // --- helper ---
    private Quiz findById(UUID id) {
        return quizRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Quiz not found"));
    }

    private QuizResponse toResponse(Quiz e) {
        return new QuizResponse(
                e.getId(),
                e.getTitle(),
                e.getDescription(),
                e.getStatus(),
                e.getSkill(),
                e.getQuizType().getId(),
                e.getQuizType().getName(),
                e.getCreatedAt(),
                e.getUpdatedAt(),
                e.getContextText(),
                e.getQuestionText(),
                e.getExplanation()
            );
        }
}
