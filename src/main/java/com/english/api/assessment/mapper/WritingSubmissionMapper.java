package com.english.api.assessment.mapper;

import com.english.api.assessment.dto.request.AICallbackWritingRequest;
import com.english.api.assessment.dto.response.WritingSubmissionResponse;
import com.english.api.assessment.model.WritingSubmission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface WritingSubmissionMapper {

    @Mapping(target = "attemptAnswerId", source = "attemptAnswer.id")
    WritingSubmissionResponse toResponse(WritingSubmission submission);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "attemptAnswer", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateFromAICallback(AICallbackWritingRequest request, @MappingTarget WritingSubmission submission);
}
