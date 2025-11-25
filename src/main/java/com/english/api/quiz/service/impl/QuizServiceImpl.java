package com.english.api.quiz.service.impl;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.quiz.dto.request.QuizCreateRequest;
import com.english.api.quiz.dto.request.QuizUpdateRequest;
import com.english.api.quiz.dto.response.PublicQuizDetailResponse;
import com.english.api.quiz.dto.response.QuizResponse;
import com.english.api.quiz.mapper.QuizMapper;
import com.english.api.quiz.model.Question;
import com.english.api.quiz.model.Quiz;
import com.english.api.quiz.model.QuizType;
import com.english.api.quiz.model.enums.QuizSkill;
import com.english.api.quiz.model.enums.QuizStatus;
import com.english.api.quiz.repository.QuizRepository;
import com.english.api.quiz.repository.QuizSectionRepository;
import com.english.api.quiz.repository.QuizTypeRepository;
import com.english.api.quiz.service.QuizService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {
    private final QuizRepository quizRepository;
    private final QuizTypeRepository quizTypeRepo;
    private final QuizSectionRepository quizSectionRepo;
    private final QuizMapper quizMapper;

    @Transactional(readOnly = true)
    public PaginationResponse search(String keyword, UUID quizTypeId, UUID quizSectionId, QuizStatus status,
                                     QuizSkill skill, Pageable pageable) {
        String trimmedKeyword = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        Page<Quiz> page = quizRepository.searchQuizzes(trimmedKeyword, quizTypeId, quizSectionId, status, skill,
                pageable);
        return PaginationResponse.from(page.map(quizMapper::toQuizListResponse), pageable);
    }

    public QuizResponse get(UUID id) {
        return quizMapper.toQuizResponse(findById(id));
    }

    public QuizResponse create(QuizCreateRequest r) {
        QuizType type = quizTypeRepo.findById(r.quizTypeId())
                .orElseThrow(() -> new EntityNotFoundException("QuizType not found"));

        Quiz q = new Quiz();
        q.setTitle(r.title());
        q.setDescription(r.description());
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
        return quizMapper.toQuizResponse(q);
    }

    public QuizResponse update(UUID id, QuizUpdateRequest r) {
        Quiz q = findById(id);

        if (r.title() != null) {
            q.setTitle(r.title());
        }
        if (r.description() != null) {
            q.setDescription(r.description());
        }
        if (r.status() != null) {
            q.setStatus(r.status());
        }
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
        if (r.contextText() != null) {
            q.setContextText(r.contextText());
        }
        if (r.questionText() != null) {
            q.setQuestionText(r.questionText());
        }
        if (r.explanation() != null) {
            q.setExplanation(r.explanation());
        }
        if (r.quizSectionId() != null) {
            var section = quizSectionRepo.findById(r.quizSectionId())
                    .orElseThrow(() -> new EntityNotFoundException("QuizSection not found"));
            q.setQuizSection(section);
        }

        quizRepository.save(q);
        return quizMapper.toQuizResponse(q);
    }

    public PaginationResponse publicQuizBySection(UUID sectionId, Pageable pageable) {
        var page = quizRepository.findByQuizSectionIdAndStatus(sectionId, QuizStatus.PUBLISHED, pageable);
        return PaginationResponse.from(page.map(quizMapper::toQuizListResponse), pageable);
    }

    public void delete(UUID id) {
        quizRepository.deleteById(id);
    }

    private Quiz findById(UUID id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Quiz not found"));
    }

    @Override
    public PaginationResponse listPublishedBySection(UUID sectionId, Pageable pageable) {
        Page<Quiz> page = quizRepository.findByQuizSectionIdAndStatus(sectionId, QuizStatus.PUBLISHED, pageable);
        return PaginationResponse.from(page.map(quizMapper::toQuizListResponse), pageable);
    }

    @Override
    public PaginationResponse publicSearch(UUID quizTypeId, UUID quizSectionId,
                                           QuizSkill skill, Pageable pageable) {
        Page<Quiz> page = quizRepository.publicSearchQuizzes(quizTypeId, quizSectionId, skill, pageable);
        return PaginationResponse.from(page.map(quizMapper::toQuizListResponse), pageable);
    }

    @Override
    public PublicQuizDetailResponse getPublicQuiz(UUID id) {
        var quiz = quizRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));
        
        QuizSkill skill = quiz.getQuizSection() != null ? quiz.getQuizSection().getSkill() : null;
        boolean shouldLoadOptions = skill != QuizSkill.SPEAKING && skill != QuizSkill.WRITING;
        
        if (shouldLoadOptions) {
            quiz = quizRepository.findWithTreeById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));
        } else {
            quiz = quizRepository.findWithTreeByIdWithoutOptions(id)
                    .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));
        }

        var deduplicatedQuestions = deduplicateQuestions(quiz.getQuestions());
        return quizMapper.toPublicQuizDetailResponse(quiz, deduplicatedQuestions);
    }

    private List<Question> deduplicateQuestions(List<Question> questions) {
        return new ArrayList<>(
                questions.stream()
                        .collect(Collectors.toMap(
                                Question::getId,
                                q -> q,
                                (existing, replacement) -> existing,
                                LinkedHashMap::new))
                        .values());
    }
}
