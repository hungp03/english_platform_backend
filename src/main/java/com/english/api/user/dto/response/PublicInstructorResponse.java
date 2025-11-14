package com.english.api.user.dto.response;
import com.english.api.course.dto.response.InstructorStatsResponse;
import java.io.Serializable;

/**
 * Public overview for an instructor page (profile + stats ONLY).
 */
public record PublicInstructorResponse(
        InstructorProfileResponse profile,
        InstructorStatsResponse stats
) implements Serializable {}
