package com.english.api.forum.service.impl;

import com.english.api.auth.util.SecurityUtil;
import com.english.api.common.dto.PaginationResponse;
import com.english.api.forum.dto.request.ForumPostCreateRequest;
import com.english.api.forum.dto.response.ForumPostResponse;
import com.english.api.forum.entity.ForumPost;
import com.english.api.forum.entity.ForumThread;
import com.english.api.forum.repo.ForumPostRepository;
import com.english.api.user.model.User;
import com.english.api.user.repository.UserRepository;
import com.english.api.forum.repo.ForumThreadRepository;
import com.english.api.forum.service.ForumPostService;
import com.english.api.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;

import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.stream.Collectors;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
@Service
@RequiredArgsConstructor
public class ForumPostServiceImpl implements ForumPostService {

  private final ForumPostRepository postRepo;
  private final ForumThreadRepository threadRepo;
  private final UserRepository userRepo;
  private final NotificationService notificationService;

  @Override
  public PaginationResponse listByThread(UUID threadId, Pageable pageable, boolean onlyPublished) {
      var thread = threadRepo.findById(threadId).orElseThrow();
  
      // 1. Lấy page bình thường như cũ
      Page<ForumPost> page = onlyPublished
              ? postRepo.findByThreadAndPublishedOrderByCreatedAtAsc(thread, true, pageable)
              : postRepo.findByThreadOrderByCreatedAtAsc(thread, pageable);
  
      var postsInPage = page.getContent();
  
      // 2. Lấy id các post trong page
      var pageIds = postsInPage.stream()
              .map(ForumPost::getId)
              .collect(Collectors.toSet());
  
      // 3. Tìm các parentId của post trong page, loại null, loại những thằng đã có trong page
      var missingParentIds = postsInPage.stream()
              .map(ForumPost::getParent)
              .filter(Objects::nonNull)
              .map(ForumPost::getId)
              .filter(parentId -> !pageIds.contains(parentId))
              .collect(Collectors.toSet());
  
      // 4. Query các parent bị thiếu
      List<ForumPost> missingParents = missingParentIds.isEmpty()
              ? Collections.emptyList()
              : postRepo.findByIdIn(missingParentIds);
  
      // Optional: sort parent cho đẹp
      missingParents.sort(Comparator.comparing(ForumPost::getCreatedAt));
  
      // 5. Map DTO: post trong page (chính)
      var mainDtos = postsInPage.stream()
              .map(this::toDto)
              .toList();
  
      // 6. Map DTO: parent bổ sung
      var parentDtos = missingParents.stream()
              .map(this::toDto)
              .toList();
  
      // 7. Gộp lại: result = [20 post trong page] + [các parent bổ sung]
      var combined = new ArrayList<>(mainDtos.size() + parentDtos.size());
      combined.addAll(mainDtos);
      combined.addAll(parentDtos);
  
      // 8. Tạo PageImpl cho DTO rồi dùng PaginationResponse.from như cũ
      Page<?> dtoPage = new PageImpl<>(
              combined,
              pageable,
              page.getTotalElements()   // tổng vẫn là tổng số post thật, không đổi
      );
  
      return PaginationResponse.from(dtoPage, pageable);
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


    // Xác định parent post nếu có
    ForumPost parentPost = null;
    if (req.parentId() != null) {
        parentPost = postRepo.findById(req.parentId()).orElse(null);
    }
    // Lấy thông tin người đang comment để hiển thị tên trong thông báo
    User currentUser = userRepo.findById(currentUserId)
            .orElseThrow(() -> new RuntimeException("User not found"));
    // 1. Gửi thông báo cho chủ thread (nếu người comment không phải chủ thread)
    if (t.getAuthorId() != null && !t.getAuthorId().equals(currentUserId)) {
      notificationService.sendNotification(
          t.getAuthorId(),
          "Phản hồi mới trong chủ đề của bạn",
          currentUser.getFullName() + " đã bình luận trong chủ đề của bạn: \"" + t.getTitle() + "\""
      );
  }

  // 2. Gửi thông báo cho chủ comment cha (nếu là reply và người reply không phải chủ comment cha)
  if (parentPost != null && parentPost.getAuthorId() != null 
      && !parentPost.getAuthorId().equals(currentUserId)) {
      // Tránh gửi trùng nếu chủ thread cũng là chủ comment cha
      if (!parentPost.getAuthorId().equals(t.getAuthorId())) {
          notificationService.sendNotification(
              parentPost.getAuthorId(),
              "Phản hồi mới trong bình luận của bạn",
              currentUser.getFullName() + " đã phản hồi trong bài post của bạn: \"" + t.getTitle() + "\""
          );
      }
  }

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

    // nếu là post cấp 1 (không có parent) -> xoá hết con rồi xoá nó
    if (p.getParent() == null) {
      postRepo.deleteByParent(p);
    }
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
