package com.english.api.forum.repository;

import com.english.api.forum.entity.ForumCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ForumCategoryRepository extends JpaRepository<ForumCategory, UUID> {
}
