package com.english.api.blog.repository;

import com.english.api.blog.model.BlogCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BlogCategoryRepository extends JpaRepository<BlogCategory, UUID> {
    boolean existsBySlug(String slug);
}
