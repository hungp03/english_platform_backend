package com.english.api.user.service;

import com.english.api.user.dto.request.CreateInstructorRequest;
import com.english.api.user.dto.request.ReviewInstructorRequest;
import com.english.api.user.dto.response.InstructorRequestListResponse;
import com.english.api.user.dto.response.InstructorRequestResponse;
import com.english.api.user.model.InstructorRequest;
import com.english.api.common.dto.PaginationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface InstructorRequestService {
    InstructorRequestResponse createInstructorRequest(UUID userId, CreateInstructorRequest request);

    InstructorRequestResponse getInstructorRequest(UUID requestId);

    PaginationResponse getAllInstructorRequests(InstructorRequest.Status status, Pageable pageable);

    InstructorRequestResponse reviewInstructorRequest(UUID requestId, ReviewInstructorRequest reviewRequest, UUID adminId);

    InstructorRequestResponse getUserCurrentRequest(UUID userId);

    boolean canUserSubmitRequest(UUID userId);

    void deleteRequest(UUID requestId);
}