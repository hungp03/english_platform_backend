package com.english.api.content.repository;

import com.english.api.content.model.ContentComment;
import com.english.api.content.model.ContentPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.transaction.Transactional;
import java.util.UUID;

public interface ContentCommentRepository extends JpaRepository<ContentComment, UUID> {

    Page<ContentComment> findByPost(ContentPost post, Pageable pageable);
    Page<ContentComment> findByPostAndPublishedIsTrue(ContentPost post, Pageable pageable);

    @Modifying
    @Transactional
    @Query("delete from ContentComment c where c.post.id = :postId")
    void deleteByPostId(@Param("postId") UUID postId);
}
