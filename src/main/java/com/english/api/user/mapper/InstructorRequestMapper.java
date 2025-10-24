package com.english.api.user.mapper;

import com.english.api.user.dto.response.InstructorRequestListResponse;
import com.english.api.user.dto.response.InstructorRequestResponse;
import com.english.api.user.dto.response.UserResponse;
import com.english.api.user.model.InstructorRequest;
import com.english.api.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InstructorRequestMapper {

    @Mapping(source = "user", target = "user", qualifiedByName = "userToUserResponse")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "reviewedBy", target = "reviewedBy", qualifiedByName = "userToUserResponse")
    InstructorRequestResponse toResponse(InstructorRequest request);

    @Named("userToUserResponse")
    default UserResponse userToUserResponse(User user) {
        if (user == null) return null;
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getAvatarUrl(),
                user.getRoles().stream().map(role -> role.getCode()).toList()
        );
    }

    InstructorRequestListResponse toListResponse(Page<InstructorRequest> requests);

    @Mapping(source = "user", target = "user", qualifiedByName = "userToSimpleUserResponse")
    @Mapping(source = "status", target = "status")
    InstructorRequestListResponse.InstructorRequestItem toItem(InstructorRequest request);

    @Named("userToSimpleUserResponse")
    default InstructorRequestListResponse.UserSimpleResponse userToSimpleUserResponse(User user) {
        if (user == null) return null;
        return new InstructorRequestListResponse.UserSimpleResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail()
        );
    }

    default InstructorRequestListResponse pageToListResponse(Page<InstructorRequest> page) {
        List<InstructorRequestListResponse.InstructorRequestItem> items = page.getContent().stream()
                .map(this::toItem)
                .toList();

        return new InstructorRequestListResponse(
                items,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.isEmpty()
        );
    }
}