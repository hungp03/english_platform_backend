package com.english.api.user.dto.response;

import com.english.api.user.model.InstructorRequest;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for InstructorRequest list (admin view)
 * Created by hungpham on 10/29/2025
 */
public class InstructorRequestListResponse {

    public record InstructorRequestItem(
            UUID id,
            UserSimpleResponse user,
            InstructorRequest.Status status,
            Instant requestedAt,
            Instant reviewedAt,
            String reviewedByName
    ) {
    }

    public record UserSimpleResponse(
            UUID id,
            String fullName,
            String email,
            String avatarUrl
    ) {
    }
}
