package com.english.api.forum.repo;
import com.english.api.forum.entity.ForumCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;
public interface ForumCategoryRepository extends JpaRepository<ForumCategory, UUID> {
  Optional<ForumCategory> findBySlug(String slug);
}
