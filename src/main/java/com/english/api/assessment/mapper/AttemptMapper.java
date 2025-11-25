package com.english.api.assessment.mapper;

import com.english.api.assessment.dto.response.*;
import com.english.api.assessment.model.QuizAttempt;
import com.english.api.assessment.model.QuizAttemptAnswer;
import com.english.api.quiz.model.QuestionOption;
import com.english.api.quiz.model.Quiz;
import com.english.api.quiz.model.enums.QuizSkill;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface AttemptMapper {

    @Mapping(target = "quizId", source = "attempt.quiz.id")
    @Mapping(target = "userId", source = "attempt.user.id")
    @Mapping(target = "quizType", source = "attempt.quiz.quizType.name")
    @Mapping(target = "quizSection", source = "attempt.quiz.quizSection.name")
    @Mapping(target = "quizName", source = "attempt.quiz.title")
    @Mapping(target = "skill", source = "attempt.skill")
    @Mapping(target = "status", expression = "java(attempt.getStatus().name())")
    @Mapping(target = "totalQuestions", source = "attempt.totalQuestions")
    @Mapping(target = "totalCorrect", source = "attempt.totalCorrect")
    @Mapping(target = "score", source = "attempt.score")
    @Mapping(target = "maxScore", source = "attempt.maxScore")
    @Mapping(target = "startedAt", source = "attempt.startedAt")
    @Mapping(target = "submittedAt", source = "attempt.submittedAt")
    @Mapping(target = "answers", source = "answerEntities")
    AttemptResponse toResponse(QuizAttempt attempt, List<QuizAttemptAnswer> answerEntities);

    @Mapping(target = "answerId", source = "id")
    @Mapping(target = "questionId", source = "question.id")
    AnswerBrief toAnswerBrief(QuizAttemptAnswer answer);

    default List<AnswerBrief> toAnswerBriefList(List<QuizAttemptAnswer> answers) {
        return answers.stream()
                .map(this::toAnswerBrief)
                .toList();
    }

    @Mapping(target = "attemptId", source = "attempt.id")
    @Mapping(target = "quizId", source = "quiz.id")
    @Mapping(target = "userId", source = "attempt.user.id")
    @Mapping(target = "quizType", source = "quiz.quizType.name")
    @Mapping(target = "quizSection", source = "quiz.quizSection.name")
    @Mapping(target = "quizName", source = "quiz.title")
    @Mapping(target = "skill", source = "attempt.skill")
    @Mapping(target = "status", expression = "java(attempt.getStatus().name())")
    @Mapping(target = "totalQuestions", source = "totalQuestions")
    @Mapping(target = "totalCorrect", source = "totalCorrect")
    @Mapping(target = "score", source = "attempt.score")
    @Mapping(target = "maxScore", source = "attempt.maxScore")
    @Mapping(target = "startedAt", source = "attempt.startedAt")
    @Mapping(target = "submittedAt", source = "attempt.submittedAt")
    @Mapping(target = "contextText", source = "quiz.contextText")
    @Mapping(target = "explanation", source = "quiz.explanation")
    @Mapping(target = "answers", source = "items")
    AttemptAnswersResponse toAttemptAnswersResponse(
            QuizAttempt attempt,
            Quiz quiz,
            Integer totalQuestions,
            Integer totalCorrect,
            List<AttemptAnswerItem> items
    );

    @Mapping(target = "questionId", source = "questionId")
    @Mapping(target = "questionContent", source = "questionContent")
    @Mapping(target = "orderIndex", source = "questionOrderIndex")
    @Mapping(target = "selectedOptionId", source = "selectedOptionId")
    @Mapping(target = "selectedOptionContent", source = "selectedOptionContent")
    @Mapping(target = "correctOptions", source = "correctOptionBriefs")
    @Mapping(target = "isCorrect", source = "isCorrect")
    @Mapping(target = "answerText", source = "answerText")
    @Mapping(target = "timeSpentMs", source = "timeSpentMs")
    @Mapping(target = "options", source = "optionReviews")
    AttemptAnswerItem toAttemptAnswerItem(
            UUID questionId,
            String questionContent,
            Integer questionOrderIndex,
            UUID selectedOptionId,
            String selectedOptionContent,
            List<OptionBrief> correctOptionBriefs,
            Boolean isCorrect,
            String answerText,
            Integer timeSpentMs,
            List<OptionReview> optionReviews
    );

    default OptionBrief toOptionBrief(QuestionOption option) {
        if (option == null) return null;
        return new OptionBrief(option.getId(), option.getContent());
    }

    default OptionReview toOptionReview(QuestionOption option, UUID selectedOptionId) {
        if (option == null) return null;
        boolean selected = selectedOptionId != null && option.getId().equals(selectedOptionId);
        return new OptionReview(option.getId(), option.getContent(), option.isCorrect(), selected);
    }

    default List<OptionReview> toOptionReviewList(List<QuestionOption> options, UUID selectedOptionId) {
        if (options == null) return List.of();
        return options.stream()
                .sorted(Comparator.comparing(QuestionOption::getOrderIndex, Comparator.nullsLast(Integer::compareTo)))
                .map(option -> toOptionReview(option, selectedOptionId))
                .toList();
    }

    default List<OptionBrief> toCorrectOptionBriefs(List<QuestionOption> options) {
        if (options == null) return List.of();
        return options.stream()
                .filter(QuestionOption::isCorrect)
                .map(this::toOptionBrief)
                .toList();
    }

    default Boolean calculateIsCorrect(UUID selectedOptionId, List<QuestionOption> correctOptions, QuizSkill skill) {
        if (skill != QuizSkill.LISTENING && skill != QuizSkill.READING) {
            return null;
        }
        if (selectedOptionId == null || correctOptions == null || correctOptions.isEmpty()) {
            return false;
        }
        return correctOptions.stream()
                .anyMatch(option -> option.getId().equals(selectedOptionId));
    }
}
