package com.english.api.course.repository;

import com.english.api.course.model.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Created by hungpham on 10/2/2025
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, UUID> {
    boolean existsBySlug(String slug);
    @Query("""
    SELECT c FROM Course c
    WHERE c.deleted = false
      AND (:keyword IS NULL OR 
           LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
      AND (:isPublished IS NULL OR c.published = :isPublished)
    """)
    Page<Course> search(@Param("keyword") String keyword,
                        @Param("isPublished") Boolean isPublished,
                        Pageable pageable);
}
