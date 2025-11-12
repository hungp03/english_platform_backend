package com.english.api.blog.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.english.api.blog.model.BlogCategory;

@Repository
public interface BlogCategoryRepository extends JpaRepository<BlogCategory, UUID> {
    boolean existsBySlug(String slug);
}
