// package com.english.api.assessment.service.impl;

// import com.english.api.assessment.dto.request.SubmitAttemptRequest;
// import com.english.api.assessment.dto.request.SubmitAnswerDto;
// import com.english.api.assessment.dto.response.AttemptResponse;
// import com.english.api.assessment.model.QuizAttempt;
// import com.english.api.assessment.model.QuizAttemptAnswer;
// import com.english.api.assessment.model.enums.QuizAttemptStatus;
// import com.english.api.assessment.repository.QuizAttemptAnswerRepository;
// import com.english.api.assessment.repository.QuizAttemptRepository;
// import com.english.api.assessment.service.AttemptService;
// import com.english.api.auth.util.SecurityUtil;
// import com.english.api.common.dto.PaginationResponse;
// import com.english.api.quiz.model.Quiz;
// import com.english.api.quiz.enums.QuizSkill;
// import com.english.api.quiz.repository.QuestionOptionRepository;
// import com.english.api.quiz.repository.QuestionRepository;
// import com.english.api.quiz.repository.QuizRepository;
// import com.english.api.user.model.User;
// import com.english.api.user.repository.UserRepository;

// import lombok.RequiredArgsConstructor;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.Pageable;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;
// import jakarta.persistence.EntityNotFoundException;
// import java.time.Instant;
// import java.util.List;
// import java.util.UUID;
// @Service
// @RequiredArgsConstructor
// public class AttemptServiceImpl implements AttemptService {
//     private final QuizAttemptRepository attemptRepo;
//     private final QuizAttemptAnswerRepository answerRepo;
//     private final QuestionOptionRepository optionRepo;
//     private final QuizRepository quizRepo;
//     private final UserRepository userRepo;
//     private final QuestionRepository questionRepo;

//     @Transactional
//     public AttemptResponse submitOneShot(SubmitAttemptRequest req) {
//         UUID userId = SecurityUtil.getCurrentUserId();
//         Quiz quiz = quizRepo.findById(req.quizId())
//                 .orElseThrow(() -> new EntityNotFoundException("Quiz not found: " + req.quizId()));

//         QuizSkill skill = quiz.getQuizSection().getSkill();

//         User userRef = userRepo.getReferenceById(userId);

//         QuizAttempt attempt = QuizAttempt.builder()
//             .quiz(quiz)
//             .user(userRef)
//             .skill(skill)
//             .status(QuizAttemptStatus.STARTED)
//             .build();

//         QuizAttempt savedAttempt = attemptRepo.save(attempt);

//         List<SubmitAnswerDto> arr = req.answers() == null ? List.of() : req.answers();
//         int total = 0;
//         int correct = 0;

//         if (skill == QuizSkill.READING || skill == QuizSkill.LISTENING) {
//             for (SubmitAnswerDto a : arr) {
//                 if (a == null || a.questionId() == null) continue;
//                 total++;

//                 var question = questionRepo.findById(a.questionId())
//                     .orElseThrow(() -> new EntityNotFoundException("Question not found: " + a.questionId()));

//                 QuizAttemptAnswer ans = answerRepo.findByAttempt_IdAndQuestion_Id(savedAttempt.getId(), a.questionId())
//                     .orElseGet(() -> QuizAttemptAnswer.of(savedAttempt, question));

//                 if (a.selectedOptionId() != null) {
//                     var opt = optionRepo.findById(a.selectedOptionId())
//                         .orElseThrow(() -> new EntityNotFoundException("Option not found: " + a.selectedOptionId()));

//                     ans.setSelectedOption(opt);
//                     if (opt.isCorrect()) correct++;
//                 } else {
//                     ans.setSelectedOption(null);
//                 }

//                 ans.setAnswerText(null);
//                 ans.setTimeSpentMs(a.timeSpentMs());
//                 answerRepo.save(ans);
//             }

//             savedAttempt.setTotalQuestions(total);
//             savedAttempt.setTotalCorrect(correct);
//             savedAttempt.setMaxScore((double) Math.max(total, 1));
//             savedAttempt.setScore((double) correct);
//             savedAttempt.setStatus(QuizAttemptStatus.AUTO_GRADED);

//         } else {
//             // WRITING / SPEAKING
//             for (SubmitAnswerDto a : arr) {
//                 if (a == null || a.questionId() == null) continue;

//                 var question = questionRepo.findById(a.questionId())
//                     .orElseThrow(() -> new EntityNotFoundException("Question not found: " + a.questionId()));

//                 QuizAttemptAnswer ans = answerRepo.findByAttempt_IdAndQuestion_Id(savedAttempt.getId(), a.questionId())
//                     .orElseGet(() -> QuizAttemptAnswer.of(savedAttempt, question));

//                 ans.setSelectedOption(null); // luôn null
//                 ans.setAnswerText(a.answerText());
//                 ans.setTimeSpentMs(a.timeSpentMs());
//                 answerRepo.save(ans);
//             }

//             savedAttempt.setStatus(QuizAttemptStatus.SUBMITTED);
//             savedAttempt.setTotalQuestions(arr.size());
//             savedAttempt.setTotalCorrect(0);
//             savedAttempt.setScore(0d);
//             savedAttempt.setMaxScore(0d);
//         }

//         savedAttempt.setSubmittedAt(Instant.now());
//         attemptRepo.save(savedAttempt);

//         return toDto(savedAttempt);
//     }

//     @Override
//     @Transactional(readOnly = true)
//     public AttemptResponse getAttempt(UUID attemptId) {
//         return attemptRepo.findById(attemptId).map(this::toDto)
//                 .orElseThrow(() -> new EntityNotFoundException("Attempt not found: " + attemptId));
//     }

//     @Override
//     @Transactional(readOnly = true)
//     public PaginationResponse listAttemptsByUser(Pageable pageable) {
//         UUID me = SecurityUtil.getCurrentUserId();
//         Page<QuizAttempt> page = attemptRepo.findByUser_Id(me, pageable);

//         return PaginationResponse.from(page.map(this::toDto), pageable);
//     }

//     @Override
//     @Transactional(readOnly = true)
//     public PaginationResponse listAttemptsByUserAndQuiz(UUID quizId, Pageable pageable) {
//         UUID me = SecurityUtil.getCurrentUserId();
//         Page<QuizAttempt> page = attemptRepo.findByQuiz_IdAndUser_Id(quizId, me, pageable);
//         return PaginationResponse.from(page.map(this::toDto), pageable);
//     }

//     @Override
//     @Transactional(readOnly = true)
//     public PaginationResponse listAttemptsByQuiz(UUID quizId, Pageable pageable) {
//         Page<QuizAttempt> page = attemptRepo.findByQuiz_Id(quizId, pageable);
//         return PaginationResponse.from(page.map(this::toDto), pageable);
//     }

//     private AttemptResponse toDto(QuizAttempt a) {
//         return new AttemptResponse(
//             a.getId(),
//             a.getQuiz().getId(),
//             a.getUser().getId(),
//             a.getQuiz().getQuizType().getName(),
//             a.getQuiz().getQuizSection().getName(),
//             a.getQuiz().getTitle(),
//             a.getSkill(),
//             a.getStatus().name(),
//             a.getTotalQuestions(),
//             a.getTotalCorrect(),
//             a.getScore(),
//             a.getMaxScore(),
//             a.getStartedAt(),
//             a.getSubmittedAt()
//         );
//     }


//     @org.springframework.transaction.annotation.Transactional(readOnly = true)
//     public com.english.api.assessment.dto.response.AttemptAnswersResponse getAttemptAnswers(java.util.UUID attemptId) {
//         var attempt = attemptRepo.findById(attemptId)
//                 .orElseThrow(() -> new IllegalArgumentException("Attempt not found"));
//         var quiz = quizRepo.findById(attempt.getQuiz().getId())
//                 .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));

//         // 1 query: answers + relations (question, selectedOption)
//         var answers = answerRepo.findByAttempt_Id(attemptId);


//         // Collect questionIds
//         var questionIds = answers.stream()
//                 .map(a -> a.getQuestion().getId())
//                 .distinct()
//                 .toList();

//         // 1 query: all options for these questions
//         var allOptions = optionRepo.findByQuestion_IdIn(questionIds);

//         // group all options by questionId
//         var allOptsByQid = allOptions.stream()
//                 .collect(java.util.stream.Collectors.groupingBy(o -> o.getQuestion().getId()));

//         java.util.List<com.english.api.assessment.dto.response.AttemptAnswerItem> items = new java.util.ArrayList<>();
//         for (var a : answers) {
//             var q = a.getQuestion();
//             // var question = a.
//             var selected = a.getSelectedOption();
//             var opts = allOptsByQid.getOrDefault(q.getId(), java.util.List.of());

//             // correct options (derive from all options)
//             var correct = opts.stream().filter(o -> o.isCorrect()).toList();

//             java.lang.Boolean isCorrect = null;
//             if (attempt.getSkill() == com.english.api.quiz.enums.QuizSkill.LISTENING
//                     || attempt.getSkill() == com.english.api.quiz.enums.QuizSkill.READING) {
//                 var selId = selected != null ? selected.getId() : null;
//                 isCorrect = selId != null && correct.stream().anyMatch(c -> c.getId().equals(selId));
//             }

//             var optionReviews = opts.stream()
//                     .sorted(java.util.Comparator.comparing(
//                         com.english.api.quiz.model.QuestionOption::getOrderIndex,
//                         java.util.Comparator.nullsLast(Integer::compareTo)))
//                     .map(o -> new com.english.api.assessment.dto.response.OptionReview(
//                             o.getId(),
//                             o.getContent(),
//                             o.isCorrect(),
//                             (selected != null && o.getId().equals(selected.getId()))
//                     ))
//                     .toList();

//             items.add(new com.english.api.assessment.dto.response.AttemptAnswerItem(
//                     q.getId(),
//                     q.getContent(),
//                     q.getOrderIndex(),
//                     selected != null ? selected.getId() : null,
//                     selected != null ? selected.getContent() : null,
//                     correct.stream()
//                            .map(c -> new com.english.api.assessment.dto.response.OptionBrief(c.getId(), c.getContent()))
//                            .toList(),
//                     isCorrect,
//                     a.getAnswerText(),
//                     a.getTimeSpentMs(),
//                     // a.getQuestion().get
//                     // quiz.getContextText(),
//                     // quiz.getExplanation(),
//                     // a.get
//                     optionReviews
//             ));
//         }

//         java.lang.Integer totalCorrect = (attempt.getSkill() == com.english.api.quiz.enums.QuizSkill.LISTENING
//                 || attempt.getSkill() == com.english.api.quiz.enums.QuizSkill.READING)
//                 ? (int) items.stream().filter(i -> java.lang.Boolean.TRUE.equals(i.isCorrect())).count()
//                 : null;

//         // var quiz = attempt.getQuiz();
//         var typeName = quiz.getQuizType() != null ? quiz.getQuizType().getName() : null;
//         var sectionName = quiz.getQuizSection() != null ? quiz.getQuizSection().getName() : null;

//         return new com.english.api.assessment.dto.response.AttemptAnswersResponse(
//                 attempt.getId(),
//                 quiz.getId(),
//                 attempt.getUser().getId(),
//                 typeName,
//                 sectionName,
//                 quiz.getTitle(),
//                 attempt.getSkill(),
//                 attempt.getStatus().name(),
//                 items.size(),
//                 totalCorrect,
//                 attempt.getScore(),
//                 attempt.getMaxScore(),
//                 attempt.getStartedAt(),
//                 attempt.getSubmittedAt(),
//                 quiz.getContextText(),
//                 quiz.getExplanation(),
//                 items
//         );
//     }

// }

package com.english.api.assessment.service.impl;

import com.english.api.assessment.dto.request.SubmitAttemptRequest;
import com.english.api.assessment.dto.request.SubmitAnswerDto;
import com.english.api.assessment.dto.response.AttemptResponse;
import com.english.api.assessment.model.QuizAttempt;
import com.english.api.assessment.model.QuizAttemptAnswer;
import com.english.api.assessment.model.enums.QuizAttemptStatus;
import com.english.api.assessment.repository.QuizAttemptAnswerRepository;
import com.english.api.assessment.repository.QuizAttemptRepository;
import com.english.api.assessment.service.AttemptService;
import com.english.api.auth.util.SecurityUtil;
import com.english.api.common.dto.PaginationResponse;
import com.english.api.quiz.model.Quiz;
import com.english.api.quiz.enums.QuizSkill;
import com.english.api.quiz.repository.QuestionOptionRepository;
import com.english.api.quiz.repository.QuestionRepository;
import com.english.api.quiz.repository.QuizRepository;
import com.english.api.user.model.User;
import com.english.api.user.repository.UserRepository;
import com.english.api.assessment.dto.response.AttemptAnswerItem;
import com.english.api.assessment.dto.response.AttemptAnswersResponse;
import com.english.api.assessment.dto.response.OptionBrief;
import com.english.api.assessment.dto.response.OptionReview;

import com.english.api.quiz.model.QuestionOption;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class AttemptServiceImpl implements AttemptService {
    private final QuizAttemptRepository attemptRepo;
    private final QuizAttemptAnswerRepository answerRepo;
    private final QuestionOptionRepository optionRepo;
    private final QuizRepository quizRepo;
    private final UserRepository userRepo;
    private final QuestionRepository questionRepo;

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
        int total = 0;
        int correct = 0;

        if (skill == QuizSkill.READING || skill == QuizSkill.LISTENING) {
            for (SubmitAnswerDto a : arr) {
                if (a == null || a.questionId() == null) continue;
                total++;

                var question = questionRepo.findById(a.questionId())
                    .orElseThrow(() -> new EntityNotFoundException("Question not found: " + a.questionId()));

                QuizAttemptAnswer ans = answerRepo.findByAttempt_IdAndQuestion_Id(savedAttempt.getId(), a.questionId())
                    .orElseGet(() -> QuizAttemptAnswer.of(savedAttempt, question));

                if (a.selectedOptionId() != null) {
                    var opt = optionRepo.findById(a.selectedOptionId())
                        .orElseThrow(() -> new EntityNotFoundException("Option not found: " + a.selectedOptionId()));

                    ans.setSelectedOption(opt);
                    if (opt.isCorrect()) correct++;
                } else {
                    ans.setSelectedOption(null);
                }

                ans.setAnswerText(null);
                ans.setTimeSpentMs(a.timeSpentMs());
                answerRepo.save(ans);
            }

            savedAttempt.setTotalQuestions(total);
            savedAttempt.setTotalCorrect(correct);
            savedAttempt.setMaxScore((double) Math.max(total, 1));
            savedAttempt.setScore((double) correct);
            savedAttempt.setStatus(QuizAttemptStatus.AUTO_GRADED);

        } else {
            // WRITING / SPEAKING
            for (SubmitAnswerDto a : arr) {
                if (a == null || a.questionId() == null) continue;

                var question = questionRepo.findById(a.questionId())
                    .orElseThrow(() -> new EntityNotFoundException("Question not found: " + a.questionId()));

                QuizAttemptAnswer ans = answerRepo.findByAttempt_IdAndQuestion_Id(savedAttempt.getId(), a.questionId())
                    .orElseGet(() -> QuizAttemptAnswer.of(savedAttempt, question));

                ans.setSelectedOption(null); // luôn null
                ans.setAnswerText(a.answerText());
                ans.setTimeSpentMs(a.timeSpentMs());
                answerRepo.save(ans);
            }

            savedAttempt.setStatus(QuizAttemptStatus.SUBMITTED);
            savedAttempt.setTotalQuestions(arr.size());
            savedAttempt.setTotalCorrect(0);
            savedAttempt.setScore(0d);
            savedAttempt.setMaxScore(0d);
        }

        savedAttempt.setSubmittedAt(Instant.now());
        attemptRepo.save(savedAttempt);

        return toDto(savedAttempt);
    }

    @Override
    @Transactional(readOnly = true)
    public AttemptResponse getAttempt(UUID attemptId) {
        return attemptRepo.findById(attemptId).map(this::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Attempt not found: " + attemptId));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse listAttemptsByUser(Pageable pageable) {
        UUID me = SecurityUtil.getCurrentUserId();
        Page<QuizAttempt> page = attemptRepo.findByUser_Id(me, pageable);

        return PaginationResponse.from(page.map(this::toDto), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse listAttemptsByUserAndQuiz(UUID quizId, Pageable pageable) {
        UUID me = SecurityUtil.getCurrentUserId();
        Page<QuizAttempt> page = attemptRepo.findByQuiz_IdAndUser_Id(quizId, me, pageable);
        return PaginationResponse.from(page.map(this::toDto), pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse listAttemptsByQuiz(UUID quizId, Pageable pageable) {
        Page<QuizAttempt> page = attemptRepo.findByQuiz_Id(quizId, pageable);
        return PaginationResponse.from(page.map(this::toDto), pageable);
    }

    private AttemptResponse toDto(QuizAttempt a) {
        return new AttemptResponse(
            a.getId(),
            a.getQuiz().getId(),
            a.getUser().getId(),
            a.getQuiz().getQuizType().getName(),
            a.getQuiz().getQuizSection().getName(),
            a.getQuiz().getTitle(),
            a.getSkill(),
            a.getStatus().name(),
            a.getTotalQuestions(),
            a.getTotalCorrect(),
            a.getScore(),
            a.getMaxScore(),
            a.getStartedAt(),
            a.getSubmittedAt()
        );
    }


    @Transactional(readOnly = true)
    public AttemptAnswersResponse getAttemptAnswers(UUID attemptId) {
        var attempt = attemptRepo.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Attempt not found"));

        var quiz = quizRepo.findById(attempt.getQuiz().getId())
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));

        // 1 query: answers + relations (question, selectedOption)
        var answers = answerRepo.findByAttempt_Id(attemptId);
        // Collect questionIds
        var questionIds = answers.stream()
                .map(a -> a.getQuestion().getId())
                .distinct()
                .toList();
        // 1 query: all options for these questions
        var allOptions = optionRepo.findByQuestion_IdIn(questionIds);

        // group all options by questionId
        var allOptsByQid = allOptions.stream()
                .collect(Collectors.groupingBy(o -> o.getQuestion().getId()));

        List<AttemptAnswerItem> items = new ArrayList<>();

        for (var a : answers) {
            var q = a.getQuestion();
            var selected = a.getSelectedOption();
            var opts = allOptsByQid.getOrDefault(q.getId(), List.of());
            var correct = opts.stream().filter(QuestionOption::isCorrect).toList();

            Boolean isCorrect = null;
            if (attempt.getSkill() == QuizSkill.LISTENING || attempt.getSkill() == QuizSkill.READING) {
                var selId = selected != null ? selected.getId() : null;
                isCorrect = selId != null && correct.stream().anyMatch(c -> c.getId().equals(selId));
            }

            var optionReviews = opts.stream()
                    .sorted(Comparator.comparing(QuestionOption::getOrderIndex, Comparator.nullsLast(Integer::compareTo)))
                    .map(o -> new OptionReview(
                            o.getId(),
                            o.getContent(),
                            o.isCorrect(),
                            selected != null && o.getId().equals(selected.getId())
                    ))
                    .toList();

            items.add(new AttemptAnswerItem(
                    q.getId(),
                    q.getContent(),
                    q.getOrderIndex(),
                    selected != null ? selected.getId() : null,
                    selected != null ? selected.getContent() : null,
                    correct.stream().map(c -> new OptionBrief(c.getId(), c.getContent())).toList(),
                    isCorrect,
                    a.getAnswerText(),
                    a.getTimeSpentMs(),
                    optionReviews
            ));
        }

        Integer totalCorrect = (attempt.getSkill() == QuizSkill.LISTENING || attempt.getSkill() == QuizSkill.READING)
                ? (int) items.stream().filter(i -> Boolean.TRUE.equals(i.isCorrect())).count()
                : null;

        var typeName = quiz.getQuizType() != null ? quiz.getQuizType().getName() : null;
        var sectionName = quiz.getQuizSection() != null ? quiz.getQuizSection().getName() : null;

        return new AttemptAnswersResponse(
                attempt.getId(),
                quiz.getId(),
                attempt.getUser().getId(),
                typeName,
                sectionName,
                quiz.getTitle(),
                attempt.getSkill(),
                attempt.getStatus().name(),
                items.size(),
                totalCorrect,
                attempt.getScore(),
                attempt.getMaxScore(),
                attempt.getStartedAt(),
                attempt.getSubmittedAt(),
                quiz.getContextText(),
                quiz.getExplanation(),
                items
        );
    }

}
