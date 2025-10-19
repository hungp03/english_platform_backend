package com.english.api.course.repository.custom;

import com.english.api.course.dto.projection.CourseWithStatsProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Custom repository for Course with native query support
 */
public interface CourseRepositoryCustom {

    Page<CourseWithStatsProjection> searchWithStats(
            String keyword,
            Boolean isPublished,
            String[] skills,
            Pageable pageable
    );

    Page<CourseWithStatsProjection> searchByOwnerWithStats(
            UUID ownerId,
            String keyword,
            Boolean isPublished,
            String[] skills,
            Pageable pageable
    );
}
