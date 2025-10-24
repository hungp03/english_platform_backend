package com.english.api.forum.repo;

import com.english.api.forum.entity.ForumThread;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ForumThreadRepository extends JpaRepository<ForumThread, UUID> {

  Optional<ForumThread> findBySlug(String slug);

  @Query("""

    SELECT DISTINCT t FROM ForumThread t
      LEFT JOIN com.english.api.forum.entity.ForumThreadCategory tc ON tc.thread = t
     WHERE (:categoryId IS NULL OR tc.category.id = :categoryId)
       AND (:locked IS NULL OR t.locked = :locked)
       AND (LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(t.bodyMd) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR :keyword IS NULL)
  """)
  Page<ForumThread> search(@Param("keyword") String keyword,
                           @Param("categoryId") UUID categoryId,
                           @Param("locked") Boolean locked,
                           Pageable pageable);
}
