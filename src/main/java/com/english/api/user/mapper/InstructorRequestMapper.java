package com.english.api.user.mapper;

import com.english.api.user.dto.response.InstructorRequestListResponse;
import com.english.api.user.dto.response.InstructorRequestResponse;
import com.english.api.user.model.InstructorRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for InstructorRequest entity
 * Created by hungpham on 10/29/2025
 */
@Mapper(componentModel = "spring", uses = {CertificateProofMapper.class})
public interface InstructorRequestMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.fullName", target = "fullName")
    @Mapping(source = "user.email", target = "email")
    @Mapping(source = "reviewedBy.id", target = "reviewedBy")
    @Mapping(source = "reviewedBy.fullName", target = "reviewedByName")
    InstructorRequestResponse toResponse(InstructorRequest instructorRequest);

    @Mapping(source = "user.id", target = "user.id")
    @Mapping(source = "user.fullName", target = "user.fullName")
    @Mapping(source = "user.email", target = "user.email")
    @Mapping(source = "user.avatarUrl", target = "user.avatarUrl")
    @Mapping(source = "reviewedBy.fullName", target = "reviewedByName")
    InstructorRequestListResponse.InstructorRequestItem toItem(InstructorRequest instructorRequest);
}
