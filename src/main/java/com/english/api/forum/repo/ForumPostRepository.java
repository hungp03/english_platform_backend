package com.english.api.forum.repo;
import com.english.api.forum.entity.ForumPost;
import com.english.api.forum.entity.ForumThread;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
public interface ForumPostRepository extends JpaRepository<ForumPost, UUID> {
  @EntityGraph(attributePaths = {"thread"})
  Page<ForumPost> findByThreadAndPublishedOrderByCreatedAtAsc(ForumThread thread, boolean published, Pageable pageable);
  Page<ForumPost> findByThreadOrderByCreatedAtAsc(ForumThread thread, Pageable pageable);
  List<ForumPost> findByIdIn(Collection<UUID> ids);
  
  @Modifying
  @Query("delete from ForumPost fp where fp.parent = :parent")
  void deleteByParent(@Param("parent") ForumPost parent);

  // 1. Tìm Post Gốc (Parent = null), có phân trang
  @Query("""
      SELECT p FROM ForumPost p
      WHERE p.thread = :thread
      AND p.parent IS NULL
      AND (:includeUnpublished = true OR p.published = true)
      ORDER BY p.createdAt ASC
  """)
  Page<ForumPost> findRootsByThread(
      @Param("thread") ForumThread thread,
      @Param("includeUnpublished") boolean includeUnpublished,
      Pageable pageable
  );

  // 2. Tìm tất cả Post Con của danh sách Parent ID
  @Query("""
      SELECT p FROM ForumPost p
      LEFT JOIN FETCH p.parent
      WHERE p.parent.id IN :parentIds
      AND (:includeUnpublished = true OR p.published = true)
      ORDER BY p.createdAt ASC
  """)
  List<ForumPost> findChildrenByParentIds(
      @Param("parentIds") List<UUID> parentIds,
      @Param("includeUnpublished") boolean includeUnpublished
  );
}
