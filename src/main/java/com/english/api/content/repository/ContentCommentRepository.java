package com.english.api.content.repository;

import com.english.api.content.model.ContentComment;
import com.english.api.content.model.ContentPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ContentCommentRepository extends JpaRepository<ContentComment, UUID> {
    Page<ContentComment> findByPostAndPublishedIsTrue(ContentPost post, Pageable pageable);
    Page<ContentComment> findByPost(ContentPost post, Pageable pageable);
}