package com.english.api.content.repository;

import com.english.api.content.model.ContentPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface ContentPostRepository extends JpaRepository<ContentPost, UUID>, JpaSpecificationExecutor<ContentPost> {
    Optional<ContentPost> findBySlugAndPublishedIsTrue(String slug);
    boolean existsBySlug(String slug);
    Optional<ContentPost> findBySlug(String slug);
}