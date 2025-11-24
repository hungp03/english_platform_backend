package com.english.api.assessment.mapper;

import com.english.api.assessment.dto.response.SpeakingSubmissionResponse;
import com.english.api.assessment.model.SpeakingSubmission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SpeakingSubmissionMapper {

    @Mapping(target = "attemptAnswerId", source = "attemptAnswer.id")
    SpeakingSubmissionResponse toResponse(SpeakingSubmission submission);
}
