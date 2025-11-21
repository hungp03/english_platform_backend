package com.english.api.forum.repository;

import com.english.api.forum.entity.ForumCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ForumCategoryRepository extends JpaRepository<ForumCategory, UUID> {
}
