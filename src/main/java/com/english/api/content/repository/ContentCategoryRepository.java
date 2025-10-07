package com.english.api.content.repository;

import com.english.api.content.model.ContentCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ContentCategoryRepository extends JpaRepository<ContentCategory, UUID> {
    boolean existsBySlug(String slug);
    Optional<ContentCategory> findBySlug(String slug);
}