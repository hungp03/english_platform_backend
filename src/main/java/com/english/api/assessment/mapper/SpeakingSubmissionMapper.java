package com.english.api.assessment.mapper;

import com.english.api.assessment.dto.request.AICallbackSpeakingRequest;
import com.english.api.assessment.dto.response.SpeakingSubmissionResponse;
import com.english.api.assessment.model.SpeakingSubmission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SpeakingSubmissionMapper {

    @Mapping(target = "attemptAnswerId", source = "attemptAnswer.id")
    SpeakingSubmissionResponse toResponse(SpeakingSubmission submission);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "attemptAnswer", ignore = true)
    @Mapping(target = "audioUrl", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateFromAICallback(AICallbackSpeakingRequest request, @MappingTarget SpeakingSubmission submission);
}
