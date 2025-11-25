package com.english.api.assessment.service.impl;

import com.english.api.assessment.dto.request.SubmitAnswerDto;
import com.english.api.assessment.dto.request.SubmitAttemptRequest;
import com.english.api.assessment.dto.response.*;
import com.english.api.assessment.event.WritingSubmissionCreatedEvent;
import com.english.api.assessment.mapper.AttemptMapper;
import com.english.api.assessment.model.QuizAttempt;
import com.english.api.assessment.model.QuizAttemptAnswer;
import com.english.api.assessment.model.WritingSubmission;
import com.english.api.assessment.model.enums.QuizAttemptStatus;
import com.english.api.assessment.repository.QuizAttemptAnswerRepository;
import com.english.api.assessment.repository.QuizAttemptRepository;
import com.english.api.assessment.repository.WritingSubmissionRepository;
import com.english.api.assessment.service.AttemptService;
import com.english.api.auth.util.SecurityUtil;
import com.english.api.common.dto.PaginationResponse;
import com.english.api.quiz.model.QuestionOption;
import com.english.api.quiz.model.Quiz;
import com.english.api.quiz.model.enums.QuizSkill;
import com.english.api.quiz.repository.QuestionOptionRepository;
import com.english.api.quiz.repository.QuestionRepository;
import com.english.api.quiz.repository.QuizRepository;
import com.english.api.user.model.User;
import com.english.api.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttemptServiceImpl implements AttemptService {
    private final QuizAttemptRepository attemptRepo;
    private final QuizAttemptAnswerRepository answerRepo;
    private final QuestionOptionRepository optionRepo;
    private final QuizRepository quizRepo;
    private final UserRepository userRepo;
    private final QuestionRepository questionRepo;
    private final WritingSubmissionRepository writingSubmissionRepo;
    private final ApplicationEventPublisher eventPublisher;
    private final AttemptMapper attemptMapper;

    @Transactional
    public AttemptResponse submitOneShot(SubmitAttemptRequest req) {
        UUID userId = SecurityUtil.getCurrentUserId();
        Quiz quiz = quizRepo.findById(req.quizId())
                .orElseThrow(() -> new EntityNotFoundException("Quiz not found: " + req.quizId()));

        QuizSkill skill = quiz.getQuizSection().getSkill();

        User userRef = userRepo.getReferenceById(userId);

        QuizAttempt attempt = QuizAttempt.builder()
                .quiz(quiz)
                .user(userRef)
                .skill(skill)
                .status(QuizAttemptStatus.STARTED)
                .build();

        QuizAttempt savedAttempt = attemptRepo.save(attempt);

        List<SubmitAnswerDto> arr = req.answers() == null ? List.of() : req.answers();
        List<QuizAttemptAnswer> savedAnswers = new ArrayList<>();
        int total = 0;
        int correct = 0;

        if (skill == QuizSkill.READING || skill == QuizSkill.LISTENING) {
            for (SubmitAnswerDto a : arr) {
                if (a == null || a.questionId() == null)
                    continue;
                total++;

                var question = questionRepo.findById(a.questionId())
                        .orElseThrow(() -> new EntityNotFoundException("Question not found: " + a.questionId()));

                QuizAttemptAnswer ans = answerRepo.findByAttempt_IdAndQuestion_Id(savedAttempt.getId(), a.questionId())
                        .orElseGet(() -> QuizAttemptAnswer.of(savedAttempt, question));

                if (a.selectedOptionId() != null) {
                    var opt = optionRepo.findById(a.selectedOptionId())
                            .orElseThrow(
                                    () -> new EntityNotFoundException("Option not found: " + a.selectedOptionId()));

                    ans.setSelectedOption(opt);
                    if (opt.isCorrect())
                        correct++;
                } else {
                    ans.setSelectedOption(null);
                }

                ans.setAnswerText(null);
                ans.setTimeSpentMs(a.timeSpentMs());
                QuizAttemptAnswer savedAns = answerRepo.save(ans);
                savedAnswers.add(savedAns);
            }

            savedAttempt.setTotalQuestions(total);
            savedAttempt.setTotalCorrect(correct);
            savedAttempt.setMaxScore((double) Math.max(total, 1));
            savedAttempt.setScore((double) correct);
            savedAttempt.setStatus(QuizAttemptStatus.AUTO_GRADED);

        } else {
            // WRITING / SPEAKING
            for (SubmitAnswerDto a : arr) {
                if (a == null || a.questionId() == null)
                    continue;

                var question = questionRepo.findById(a.questionId())
                        .orElseThrow(() -> new EntityNotFoundException("Question not found: " + a.questionId()));

                QuizAttemptAnswer ans = answerRepo.findByAttempt_IdAndQuestion_Id(savedAttempt.getId(), a.questionId())
                        .orElseGet(() -> QuizAttemptAnswer.of(savedAttempt, question));

                ans.setSelectedOption(null); // luôn null
                ans.setAnswerText(a.answerText());
                ans.setTimeSpentMs(a.timeSpentMs());
                QuizAttemptAnswer savedAns = answerRepo.save(ans);
                savedAnswers.add(savedAns);
            }

            savedAttempt.setStatus(QuizAttemptStatus.SUBMITTED);
            savedAttempt.setTotalQuestions(arr.size());
            savedAttempt.setTotalCorrect(0);
            savedAttempt.setScore(0d);
            savedAttempt.setMaxScore(0d);
            // Auto-create WritingSubmission for WRITING skill
            if (skill == QuizSkill.WRITING) {
                for (QuizAttemptAnswer answer : savedAnswers) {
                    // Check if writing text exists
                    if (answer.getAnswerText() != null && !answer.getAnswerText().isBlank()) {
                        // Check if submission already exists
                        if (!writingSubmissionRepo.existsByAttemptAnswer_Id(answer.getId())) {
                            // Create WritingSubmission
                            WritingSubmission submission = WritingSubmission.builder()
                                    .attemptAnswer(answer)
                                    .build();

                            WritingSubmission saved = writingSubmissionRepo.save(submission);

                            // Publish event - trigger will run after transaction commits
                            eventPublisher.publishEvent(new WritingSubmissionCreatedEvent(saved.getId()));

                            log.info("Auto-created WritingSubmission for answer: {}", answer.getId());
                        }
                    }
                }
            }
        }
        savedAttempt.setSubmittedAt(Instant.now());
        attemptRepo.save(savedAttempt);
        return attemptMapper.toResponse(savedAttempt, savedAnswers);
    }

    @Override
    @Transactional(readOnly = true)
    public AttemptResponse getAttempt(UUID attemptId) {
        QuizAttempt attempt = attemptRepo.findById(attemptId)
                .orElseThrow(() -> new EntityNotFoundException("Attempt not found: " + attemptId));
        List<QuizAttemptAnswer> answers = answerRepo.findByAttempt_Id(attemptId);
        return attemptMapper.toResponse(attempt, answers);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse listAttemptsByUser(Pageable pageable) {
        UUID me = SecurityUtil.getCurrentUserId();
        Page<QuizAttempt> page = attemptRepo.findByUser_Id(me, pageable);

        return PaginationResponse.from(page.map(attempt -> {
            List<QuizAttemptAnswer> answers = answerRepo.findByAttempt_Id(attempt.getId());
            return attemptMapper.toResponse(attempt, answers);
        }), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse listAttemptsByUserAndQuiz(UUID quizId, Pageable pageable) {
        UUID me = SecurityUtil.getCurrentUserId();
        Page<QuizAttempt> page = attemptRepo.findByQuiz_IdAndUser_Id(quizId, me, pageable);
        return PaginationResponse.from(page.map(attempt -> {
            List<QuizAttemptAnswer> answers = answerRepo.findByAttempt_Id(attempt.getId());
            return attemptMapper.toResponse(attempt, answers);
        }), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse listAttemptsByQuiz(UUID quizId, Pageable pageable) {
        Page<QuizAttempt> page = attemptRepo.findByQuiz_Id(quizId, pageable);
        return PaginationResponse.from(page.map(attempt -> {
            List<QuizAttemptAnswer> answers = answerRepo.findByAttempt_Id(attempt.getId());
            return attemptMapper.toResponse(attempt, answers);
        }), pageable);
    }

    @Transactional(readOnly = true)
    public AttemptAnswersResponse getAttemptAnswers(UUID attemptId) {
        QuizAttempt attempt = attemptRepo.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Attempt not found"));

        UUID quizId = attempt.getQuiz().getId();
        Quiz quiz = quizRepo.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));

        List<QuizAttemptAnswer> answers = answerRepo.findByAttempt_Id(attemptId);

        List<UUID> questionIds = answers.stream()
                .map(answer -> answer.getQuestion().getId())
                .distinct()
                .toList();

        List<QuestionOption> allOptions = optionRepo.findByQuestion_IdIn(questionIds);

        Map<UUID, List<QuestionOption>> optionsByQuestionId = allOptions.stream()
                .collect(Collectors.groupingBy(option -> option.getQuestion().getId()));

        QuizSkill skill = attempt.getSkill();
        boolean isGradableSkill = (skill == QuizSkill.LISTENING || skill == QuizSkill.READING);

        List<AttemptAnswerItem> items = answers.stream()
                .map(answer -> buildAttemptAnswerItem(answer, optionsByQuestionId, isGradableSkill))
                .toList();

        Integer totalCorrect = isGradableSkill
                ? (int) items.stream().filter(item -> Boolean.TRUE.equals(item.isCorrect())).count()
                : null;

        return attemptMapper.toAttemptAnswersResponse(
                attempt,
                quiz,
                items.size(),
                totalCorrect,
                items);
    }

    private AttemptAnswerItem buildAttemptAnswerItem(
            QuizAttemptAnswer answer,
            Map<UUID, List<QuestionOption>> optionsByQuestionId,
            boolean isGradableSkill) {

        UUID questionId = answer.getQuestion().getId();
        String questionContent = answer.getQuestion().getContent();
        Integer questionOrderIndex = answer.getQuestion().getOrderIndex();

        QuestionOption selectedOption = answer.getSelectedOption();
        UUID selectedOptionId = selectedOption != null ? selectedOption.getId() : null;
        String selectedOptionContent = selectedOption != null ? selectedOption.getContent() : null;

        List<QuestionOption> questionOptions = optionsByQuestionId.getOrDefault(questionId, List.of());

        List<QuestionOption> correctOptions = questionOptions.stream()
                .filter(QuestionOption::isCorrect)
                .toList();

        Boolean isCorrect = attemptMapper.calculateIsCorrect(selectedOptionId, correctOptions, 
                answer.getAttempt().getSkill());

        List<OptionReview> optionReviews = attemptMapper.toOptionReviewList(questionOptions, selectedOptionId);

        List<OptionBrief> correctOptionBriefs = attemptMapper.toCorrectOptionBriefs(correctOptions);

        return attemptMapper.toAttemptAnswerItem(
                questionId,
                questionContent,
                questionOrderIndex,
                selectedOptionId,
                selectedOptionContent,
                correctOptionBriefs,
                isCorrect,
                answer.getAnswerText(),
                answer.getTimeSpentMs(),
                optionReviews);
    }

}
