package com.english.api.course.dto.response;

import java.util.UUID;

/**
 * Created by hungpham on 10/4/2025
 */
public record CourseModuleUpdateResponse(
        UUID id,
        String title,
        Integer position
        ) {
}
