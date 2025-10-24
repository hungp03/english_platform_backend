package com.english.api.forum.service.impl;

import com.english.api.auth.util.SecurityUtil;
import com.english.api.common.dto.PaginationResponse;
import com.english.api.forum.dto.request.ForumPostCreateRequest;
import com.english.api.forum.dto.response.ForumPostResponse;
import com.english.api.forum.entity.ForumPost;
import com.english.api.forum.entity.ForumThread;
import com.english.api.forum.repo.ForumPostRepository;
import com.english.api.user.repository.UserRepository;
import com.english.api.forum.repo.ForumThreadRepository;
import com.english.api.forum.service.ForumPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ForumPostServiceImpl implements ForumPostService {

  private final ForumPostRepository postRepo;
  private final ForumThreadRepository threadRepo;
  private final UserRepository userRepo;

  @Override
  public PaginationResponse listByThread(UUID threadId, Pageable pageable, boolean onlyPublished) {
    var t = threadRepo.findById(threadId).orElseThrow();
    Page<ForumPost> page = onlyPublished
        ? postRepo.findByThreadAndPublishedOrderByCreatedAtAsc(t, true, pageable)
        : postRepo.findByThreadOrderByCreatedAtAsc(t, pageable);
    var mapped = page.getContent().stream().map(this::toDto).toList();
    return PaginationResponse.from(new PageImpl<>(mapped, pageable, page.getTotalElements()), pageable);
  }

  @Override
  @Transactional
  public ForumPostResponse create(UUID threadId, ForumPostCreateRequest req) {
    UUID currentUserId = SecurityUtil.getCurrentUserId();
    var t = threadRepo.findById(threadId).orElseThrow();

    if (t.isLocked()) {
      throw new IllegalStateException("Thread is locked");
    }

    var p = ForumPost.builder()
        .thread(t)
        .parent(req.parentId() == null ? null : ForumPost.builder().id(req.parentId()).build())
        .authorId(currentUserId)
        .bodyMd(req.bodyMd())
        .published(true)
        .build();

    p = postRepo.save(p);

    t.setReplyCount(t.getReplyCount() + 1);
    t.setLastPostAt(Instant.now());
    t.setLastPostId(p.getId());
    t.setLastPostAuthor(currentUserId);
    threadRepo.save(t);

    return toDto(p);
  }

  @Override
  @Transactional
  public ForumPostResponse hide(UUID postId) {
    var p = postRepo.findById(postId).orElseThrow();
    p.setPublished(false);
    p = postRepo.save(p);
    return toDto(p);
  }

  @Override
  @Transactional
  public ForumPostResponse show(UUID postId) {
    var p = postRepo.findById(postId).orElseThrow();
    p.setPublished(true);
    p = postRepo.save(p);
    return toDto(p);
  }

  private ForumPostResponse toDto(ForumPost p) {
    String authorName = null;
    String authorAvatarUrl = null;
  
    if (p.getAuthorId() != null) {
      var u = userRepo.findById(p.getAuthorId()).orElse(null);
      if (u != null) {
        authorName = u.getFullName();
        authorAvatarUrl = u.getAvatarUrl();
      }
    }
  
    return new ForumPostResponse(
        p.getId(),
        p.getThread() != null ? p.getThread().getId() : null,
        p.getParent() != null ? p.getParent().getId() : null,
        p.getAuthorId(),
        authorName,
        authorAvatarUrl,
        p.getBodyMd(),
        p.isPublished(),
        p.getCreatedAt(),
        p.getUpdatedAt()
    );
  }
  

  @Override
  @Transactional
  public void deleteByOwner(java.util.UUID postId) {
    java.util.UUID uid = com.english.api.auth.util.SecurityUtil.getCurrentUserId();
    var p = postRepo.findById(postId).orElseThrow();
    if (p.getAuthorId() == null || !p.getAuthorId().equals(uid)) {
      throw new org.springframework.security.access.AccessDeniedException("Only author can delete this post");
    }
    // adjust thread counters
    var t = p.getThread();
    postRepo.delete(p);
    if (t != null) {
      long rc = Math.max(0, t.getReplyCount() - 1);
      t.setReplyCount(rc);
      threadRepo.save(t);
    }
  }

  @Override
  @Transactional
  public void adminDelete(java.util.UUID postId) {
    var p = postRepo.findById(postId).orElseThrow();
    var t = p.getThread();
    postRepo.delete(p);
    if (t != null) {
      long rc = Math.max(0, t.getReplyCount() - 1);
      t.setReplyCount(rc);
      threadRepo.save(t);
    }
  }
}
