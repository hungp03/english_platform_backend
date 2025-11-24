package com.english.api.assessment.mapper;

import com.english.api.assessment.dto.response.WritingSubmissionResponse;
import com.english.api.assessment.model.WritingSubmission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WritingSubmissionMapper {

    @Mapping(target = "attemptAnswerId", source = "attemptAnswer.id")
    WritingSubmissionResponse toResponse(WritingSubmission submission);
}
