package com.english.api.quiz.service.impl;
import com.english.api.common.dto.PaginationResponse;
// import com.english.api.common.PaginationResponse;
// import com.english.api.quiz.dto.*;
import com.english.api.quiz.dto.request.QuizCreateRequest;
import com.english.api.quiz.dto.request.QuizUpdateRequest;
import com.english.api.quiz.dto.response.PublicOption;
import com.english.api.quiz.dto.response.PublicQuestion;
import com.english.api.quiz.dto.response.PublicQuizDetailResponse;
import com.english.api.quiz.dto.response.QuizListResponse;
import com.english.api.quiz.dto.response.QuizResponse;
import com.english.api.quiz.model.*;
import com.english.api.quiz.repository.*;
import com.english.api.quiz.enums.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements com.english.api.quiz.service.QuizService {
    private final QuizRepository quizRepository;
    private final QuizTypeRepository quizTypeRepo;
    private final QuizSectionRepository quizSectionRepo;

    @Transactional(readOnly = true)
    public PaginationResponse search(String keyword, UUID quizTypeId, UUID quizSectionId, QuizStatus status, QuizSkill skill, Pageable pageable) {
        Specification<Quiz> spec = (root, query, cb) -> {
            // Only add fetch if NOT a count query
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("quizType", JoinType.LEFT);
                root.fetch("quizSection", JoinType.LEFT);
                query.distinct(true);
            }
            return null;
        };
        
        if (keyword != null && !keyword.isBlank()) {
            Specification<Quiz> s = (root, query, cb) -> {
                // Use join instead of fetch for filtering
                return cb.or(
                    cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase().trim() + "%"),
                    cb.like(cb.lower(root.get("description")), "%" + keyword.toLowerCase().trim() + "%")
                );
            };
            spec = spec.and(s);
        }
        
        if (quizTypeId != null) {
            Specification<Quiz> s = (root, query, cb) -> {
                if (query.getResultType() == Long.class || query.getResultType() == long.class) {
                    // For count queries, use join
                    var join = root.join("quizType", JoinType.LEFT);
                    return cb.equal(join.get("id"), quizTypeId);
                }
                return cb.equal(root.get("quizType").get("id"), quizTypeId);
            };
            spec = spec.and(s);
        }
        
        if (quizSectionId != null) {
            Specification<Quiz> s = (root, query, cb) -> {
                if (query.getResultType() == Long.class || query.getResultType() == long.class) {
                    var join = root.join("quizSection", JoinType.LEFT);
                    return cb.equal(join.get("id"), quizSectionId);
                }
                return cb.equal(root.get("quizSection").get("id"), quizSectionId);
            };
            spec = spec.and(s);
        }
        
        if (status != null) {
            Specification<Quiz> s = (root, query, cb) -> cb.equal(root.get("status"), status);
            spec = spec.and(s);
        }
        
        if (skill != null) {
            Specification<Quiz> s = (root, query, cb) -> {
                var section = root.join("quizSection", JoinType.LEFT);
                return cb.equal(section.get("skill"), skill);
            };
            spec = spec.and(s);
        }
        
        Page<Quiz> page = quizRepository.findAll(spec, pageable);
        return PaginationResponse.from(page.map(this::toListResponse), pageable);
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
        // q.setSkill(r.skill());
        q.setStatus(r.status() == null ? QuizStatus.DRAFT : r.status());
        q.setQuizType(type);
        q.setContextText(r.contextText());
        q.setQuestionText(r.questionText());
        q.setExplanation(r.explanation());
        if (r.quizSectionId() != null) {
            var section = quizSectionRepo.findById(r.quizSectionId())
                    .orElseThrow(() -> new EntityNotFoundException("QuizSection not found"));
            q.setQuizSection(section);
        }
        quizRepository.save(q);
        return toResponse(q);
    }

    public QuizResponse update(UUID id, QuizUpdateRequest r) {
        Quiz q = findById(id);
        if (r.title() != null) q.setTitle(r.title());
        if (r.description() != null) q.setDescription(r.description());
        // if (r.skill() != null) q.setSkill(r.skill());
        if (r.status() != null) q.setStatus(r.status());
        if (r.quizTypeId() != null) {
            QuizType type = quizTypeRepo.findById(r.quizTypeId())
                    .orElseThrow(() -> new EntityNotFoundException("QuizType not found"));
            q.setQuizType(type);
        }
        if (r.quizSectionId() != null) {
            var section = quizSectionRepo.findById(r.quizSectionId())
                .orElseThrow(() -> new EntityNotFoundException("QuizSection not found"));
            q.setQuizSection(section);
        }
        if (r.quizSectionId() != null) {
            var section = quizSectionRepo.findById(r.quizSectionId())
                .orElseThrow(() -> new EntityNotFoundException("QuizSection not found"));
            q.setQuizSection(section);
        }
        if (r.contextText() != null) q.setContextText(r.contextText());
        if (r.questionText() != null) q.setQuestionText(r.questionText());
        if (r.explanation() != null) q.setExplanation(r.explanation());
        if (r.quizSectionId() != null) {
            var section = quizSectionRepo.findById(r.quizSectionId())
                    .orElseThrow(() -> new EntityNotFoundException("QuizSection not found"));
            q.setQuizSection(section);
        }
        quizRepository.save(q);
        return toResponse(q);
    }
    public PaginationResponse publicQuizBySection(UUID sectionId, Pageable pageable){
        var page = quizRepository.findByQuizSectionIdAndStatus(sectionId, QuizStatus.PUBLISHED, pageable);
        return PaginationResponse.from(page.map(this::toListResponse), pageable);
    }

    public void delete(UUID id) {
        quizRepository.deleteById(id);
    }

    // --- helper ---
    private Quiz findById(UUID id) {
        return quizRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Quiz not found"));
    }

    @Override
    public PaginationResponse listPublishedBySection(UUID sectionId, Pageable pageable) {
        Specification<Quiz> spec = (root, q, cb) -> {
            var predicates = new ArrayList<Predicate>();
            predicates.add(cb.equal(root.get("status"), QuizStatus.PUBLISHED));
            Join<Object, Object> section = root.join("quizSection", JoinType.LEFT);
            predicates.add(cb.equal(section.get("id"), sectionId));
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<Quiz> page = quizRepository.findAll(spec, pageable);
        Page<QuizListResponse> mapped = page.map(this::toListResponse);
        return PaginationResponse.from(mapped, pageable);
    }

    @Override
    public PaginationResponse publicSearch(UUID quizTypeId, UUID quizSectionId, QuizSkill skill, Pageable pageable) {
        Specification<Quiz> spec = (root, q, cb) -> {
            var predicates = new ArrayList<Predicate>();
            // Chỉ trả về PUBLISHED cho public
            predicates.add(cb.equal(root.get("status"), QuizStatus.PUBLISHED));

            // join type / section khi cần filter
            if (quizTypeId != null) {
                Join<Object, Object> type = root.join("quizType", JoinType.LEFT);
                predicates.add(cb.equal(type.get("id"), quizTypeId));
            }
            if (quizSectionId != null) {
                Join<Object, Object> section = root.join("quizSection", JoinType.LEFT);
                predicates.add(cb.equal(section.get("id"), quizSectionId));
            }
            if (skill != null) {
                // skill nằm trên QuizSection
                Join<Object, Object> section = root.join("quizSection", JoinType.LEFT);
                predicates.add(cb.equal(section.get("skill"), skill));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Quiz> page = quizRepository.findAll(spec, pageable);
        Page<QuizListResponse> mapped = page.map(this::toListResponse);
        return PaginationResponse.from(mapped, pageable);
    }



    private QuizResponse toResponse(Quiz e) {
        return new QuizResponse(
                e.getId(),
                e.getTitle(),
                e.getDescription(),
                e.getStatus(),
                // e.getSkill(),
                (e.getQuizSection() != null ? e.getQuizSection().getSkill() : null),
                e.getQuizType().getId(),
                (e.getQuizSection() != null ? e.getQuizSection().getId() : null),
                (e.getQuizSection() != null ? e.getQuizSection().getName() : null),
                e.getQuizType().getName(),
                e.getCreatedAt(),
                e.getUpdatedAt(),
                e.getContextText(),
                e.getQuestionText(),
                e.getExplanation()
            );
        }
    private QuizListResponse toListResponse(Quiz e) {
        return new QuizListResponse(
                e.getId(),
                e.getTitle(),
                e.getStatus(),
                // e.getSkill(),
                (e.getQuizSection() != null ? e.getQuizSection().getSkill() : null),
                e.getQuizType().getId(),
                (e.getQuizSection() != null ? e.getQuizSection().getId() : null),
                (e.getQuizSection() != null ? e.getQuizSection().getName() : null),
                e.getQuizType().getName(),
                e.getCreatedAt(),
                e.getUpdatedAt()
            );
        }

    @Override
    public PublicQuizDetailResponse getPublicQuiz(UUID id) {
        var quiz = quizRepository.findWithTreeById(id)
            .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));

        var section = quiz.getQuizSection();
        var type = quiz.getQuizType();

        // ✅ GIẢI PHÁP: Deduplicate questions bằng LinkedHashMap
        // Key = question.id, Value = question
        var uniqueQuestions = quiz.getQuestions().stream()
            .collect(Collectors.toMap(
                Question::getId,           // key
                q -> q,                    // value
                (existing, replacement) -> existing,  // merge function: giữ question đầu tiên
                LinkedHashMap::new         // maintain insertion order
            ))
            .values()
            .stream()
            .map(q -> new PublicQuestion(
                q.getId(),
                q.getOrderIndex(),
                q.getContent(),
                q.getOptions().stream()
                    .map(o -> new PublicOption(
                        o.getId(),
                        o.getContent(),
                        o.getOrderIndex()
                    ))
                    .toList()
            ))
            .toList();

        return new PublicQuizDetailResponse(
            quiz.getId(),
            quiz.getTitle(),
            quiz.getDescription(),
            type != null ? type.getId() : null,
            type != null ? type.getName() : null,
            section != null ? section.getId() : null,
            section != null ? section.getName() : null,
            quiz.getContextText(),
            section != null ? section.getSkill() : null,
            uniqueQuestions
        );
    }

}
