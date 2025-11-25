package com.english.api.assessment.mapper;

import com.english.api.assessment.dto.request.AICallbackSpeakingRequest;
import com.english.api.assessment.dto.response.SpeakingSubmissionResponse;
import com.english.api.assessment.dto.response.SpeakingSubmissionsWithMetadataResponse;
import com.english.api.assessment.model.QuizAttempt;
import com.english.api.assessment.model.SpeakingSubmission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SpeakingSubmissionMapper {

    @Mapping(target = "attemptAnswerId", source = "attemptAnswer.id")
    SpeakingSubmissionResponse toResponse(SpeakingSubmission submission);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "attemptAnswer", ignore = true)
    @Mapping(target = "audioUrl", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateFromAICallback(AICallbackSpeakingRequest request, @MappingTarget SpeakingSubmission submission);

    @Mapping(target = "attemptId", source = "attempt.id")
    @Mapping(target = "quizId", source = "attempt.quiz.id")
    @Mapping(target = "quizType", source = "attempt.quiz.quizType.name")
    @Mapping(target = "quizSection", source = "attempt.quiz.quizSection.name")
    @Mapping(target = "quizName", source = "attempt.quiz.title")
    @Mapping(target = "skill", source = "attempt.skill")
    @Mapping(target = "status", expression = "java(attempt.getStatus().name())")
    @Mapping(target = "totalQuestions", source = "attempt.totalQuestions")
    @Mapping(target = "startedAt", source = "attempt.startedAt")
    @Mapping(target = "submittedAt", source = "attempt.submittedAt")
    @Mapping(target = "contextText", source = "attempt.quiz.contextText")
    @Mapping(target = "submissions", source = "submissions")
    SpeakingSubmissionsWithMetadataResponse toSubmissionsWithMetadataResponse(QuizAttempt attempt, List<SpeakingSubmissionResponse> submissions);
}
