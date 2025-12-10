package com.english.api.user.dto.response;
import com.english.api.course.dto.response.PublicInstructorStatsResponse;

import java.io.Serializable;

/**
 * Public overview for an instructor page (profile + stats ONLY).
 */
public record PublicInstructorResponse(
        InstructorProfileResponse profile,
        PublicInstructorStatsResponse stats
) implements Serializable {}
