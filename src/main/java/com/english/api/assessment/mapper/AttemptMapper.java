package com.english.api.assessment.mapper;

import com.english.api.assessment.dto.response.AnswerBrief;
import com.english.api.assessment.dto.response.AttemptResponse;
import com.english.api.assessment.model.QuizAttempt;
import com.english.api.assessment.model.QuizAttemptAnswer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

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
}
