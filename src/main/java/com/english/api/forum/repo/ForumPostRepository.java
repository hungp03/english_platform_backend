package com.english.api.forum.repo;
import com.english.api.forum.entity.ForumPost;
import com.english.api.forum.entity.ForumThread;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface ForumPostRepository extends JpaRepository<ForumPost, UUID> {
  Page<ForumPost> findByThreadAndPublishedOrderByCreatedAtAsc(ForumThread thread, boolean published, Pageable pageable);
  Page<ForumPost> findByThreadOrderByCreatedAtAsc(ForumThread thread, Pageable pageable);
}
