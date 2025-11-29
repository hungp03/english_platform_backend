package com.english.api.quiz.service.impl;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.common.exception.ResourceNotFoundException;
import com.english.api.quiz.dto.request.QuizCreateRequest;
import com.english.api.quiz.dto.request.QuizUpdateRequest;
import com.english.api.quiz.dto.response.PublicQuizDetailResponse;
import com.english.api.quiz.dto.response.QuizResponse;
import com.english.api.quiz.mapper.QuizMapper;
import com.english.api.quiz.model.Question;
import com.english.api.quiz.model.Quiz;
import com.english.api.quiz.model.QuizSection;
import com.english.api.quiz.model.QuizType;
import com.english.api.quiz.model.enums.QuizSkill;
import com.english.api.quiz.model.enums.QuizStatus;
import com.english.api.quiz.repository.QuizRepository;
import com.english.api.quiz.repository.QuizSectionRepository;
import com.english.api.quiz.repository.QuizTypeRepository;
import com.english.api.quiz.service.QuizService;
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

    public QuizResponse create(QuizCreateRequest request) {
        QuizType type = quizTypeRepo.findById(request.quizTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("QuizType not found"));

        Quiz quiz = new Quiz();
        quiz.setTitle(request.title());
        quiz.setDescription(request.description());
        quiz.setStatus(request.status() == null ? QuizStatus.DRAFT : request.status());
        quiz.setQuizType(type);
        quiz.setContextText(request.contextText());
        quiz.setQuestionText(request.questionText());
        quiz.setExplanation(request.explanation());

        if (request.quizSectionId() != null) {
            QuizSection section = quizSectionRepo.findById(request.quizSectionId())
                    .orElseThrow(() -> new ResourceNotFoundException("QuizSection not found"));
            quiz.setQuizSection(section);
        }

        quizRepository.save(quiz);
        return quizMapper.toQuizResponse(quiz);
    }

    public QuizResponse update(UUID id, QuizUpdateRequest request) {
        Quiz quiz = findById(id);

        if (request.title() != null) {
            quiz.setTitle(request.title());
        }
        if (request.description() != null) {
            quiz.setDescription(request.description());
        }
        if (request.status() != null) {
            quiz.setStatus(request.status());
        }
        if (request.quizTypeId() != null) {
            QuizType type = quizTypeRepo.findById(request.quizTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("QuizType not found"));
            quiz.setQuizType(type);
        }
        if (request.quizSectionId() != null) {
            QuizSection section = quizSectionRepo.findById(request.quizSectionId())
                    .orElseThrow(() -> new ResourceNotFoundException("QuizSection not found"));
            quiz.setQuizSection(section);
        }
        if (request.contextText() != null) {
            quiz.setContextText(request.contextText());
        }
        if (request.questionText() != null) {
            quiz.setQuestionText(request.questionText());
        }
        if (request.explanation() != null) {
            quiz.setExplanation(request.explanation());
        }

        if (request.explanation() != null) {
            quiz.setExplanation(request.explanation());
        }

        quizRepository.save(quiz);
        return quizMapper.toQuizResponse(quiz);
    }

    public PaginationResponse publicQuizBySection(UUID sectionId, Pageable pageable) {
        Page<Quiz> page = quizRepository.findByQuizSectionIdAndStatus(sectionId, QuizStatus.PUBLISHED, pageable);
        return PaginationResponse.from(page.map(quizMapper::toQuizListResponse), pageable);
    }

    public void delete(UUID id) {
        quizRepository.deleteById(id);
    }

    private Quiz findById(UUID id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));
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
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));
        
        QuizSkill skill = quiz.getQuizSection() != null ? quiz.getQuizSection().getSkill() : null;
        boolean shouldLoadOptions = skill != QuizSkill.SPEAKING && skill != QuizSkill.WRITING;
        
        if (shouldLoadOptions) {
            quiz = quizRepository.findWithTreeById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));
        } else {
            quiz = quizRepository.findWithTreeByIdWithoutOptions(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));
        }

        List<Question> deduplicatedQuestions = deduplicateQuestions(quiz.getQuestions());
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
