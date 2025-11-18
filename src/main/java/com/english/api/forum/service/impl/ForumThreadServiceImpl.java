package com.english.api.forum.service.impl;

import com.english.api.auth.util.SecurityUtil;
import com.english.api.common.dto.PaginationResponse;
import com.english.api.common.exception.AccessDeniedException;
import com.english.api.forum.dto.request.ForumThreadCreateRequest;
import com.english.api.forum.dto.response.ForumCategoryResponse;
import com.english.api.forum.dto.response.ForumThreadResponse;
import com.english.api.forum.dto.response.ForumThreadListResponse;
import com.english.api.forum.entity.ForumCategory;
import com.english.api.forum.entity.ForumThread;
import com.english.api.forum.entity.ForumThreadCategory;
import com.english.api.forum.repo.ForumCategoryRepository;
import com.english.api.forum.repo.ForumThreadCategoryRepository;
import com.english.api.forum.repo.ForumThreadRepository;
import com.english.api.forum.service.ForumThreadService;
import com.english.api.forum.util.SlugUtil;
import com.english.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ForumThreadServiceImpl implements ForumThreadService {

  private final ForumThreadRepository threadRepo;
  private final UserRepository userRepo;
  private final ForumCategoryRepository categoryRepo;
  private final ForumThreadCategoryRepository threadCatRepo;

  @Override
  public PaginationResponse listPublic(String keyword, UUID categoryId, Boolean locked, Pageable pageable) {
    String normalizedKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
    Page<ForumThread> page = threadRepo.search(normalizedKeyword, categoryId, locked, pageable);
    var threadListResponses = page.getContent().stream().map(this::toListDto).toList();
    return PaginationResponse.from(new PageImpl<>(threadListResponses, pageable, page.getTotalElements()), pageable);
  }

  @Override
  public ForumThreadResponse getBySlug(String slug) {
    var thread = threadRepo.findBySlug(slug).orElseThrow();
    return toDto(thread);
  }

  @Override
  @Async
  @Transactional
  public void increaseView(UUID threadId) {
    try {
      var thread = threadRepo.findById(threadId).orElseThrow();
      thread.setViewCount(thread.getViewCount() + 1);
      threadRepo.save(thread);
    } catch (Exception e) {
      // Log error but don't fail the main request
      // View count update is non-critical
      log.error("Failed to increase view count for thread {}: {}", threadId, e.getMessage(), e);
    }
  }

  @Override
  @Transactional
  public ForumThreadResponse create(ForumThreadCreateRequest request) {
    UUID currentUserId = SecurityUtil.getCurrentUserId();
    String base = (request.title() == null ? "" : request.title());
    String slug = SlugUtil.ensureUnique(SlugUtil.slugify(base),
        s -> threadRepo.findBySlug(s).isPresent());

    var thread = ForumThread.builder()
        .authorId(currentUserId)
        .title(request.title())
        .slug(slug)
        .bodyMd(request.bodyMd())
        .locked(false)
        .viewCount(0)
        .replyCount(0)
        .lastPostAt(Instant.now())
        .build();
    thread = threadRepo.save(thread);

    if (request.categoryIds() != null && !request.categoryIds().isEmpty()) {
      List<ForumCategory> categories = categoryRepo.findAllById(request.categoryIds());
      for (ForumCategory category : categories) {
        threadCatRepo.save(ForumThreadCategory.builder().thread(thread).category(category).build());
      }
    }
    return toDto(thread);
  }

  @Override
  @Transactional
  public ForumThreadResponse adminLock(UUID id, boolean lock) {
    var thread = threadRepo.findById(id).orElseThrow();
    thread.setLocked(lock);
    thread = threadRepo.save(thread);
    return toDto(thread);
  }

  @Override
  @Transactional
  public ForumThreadResponse lockByOwner(UUID id, boolean lock) {
    UUID currentUserId = SecurityUtil.getCurrentUserId();
    var thread = threadRepo.findById(id).orElseThrow();
    if (thread.getAuthorId() == null || !thread.getAuthorId().equals(currentUserId)) {
      throw new AccessDeniedException("Only thread owner can lock/unlock this thread");
    }
    thread.setLocked(lock);
    thread = threadRepo.save(thread);
    return toDto(thread);
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    threadRepo.deleteById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public PaginationResponse listByAuthor(UUID authorId, Pageable pageable) {
      // UUID currentUserId = SecurityUtil.getCurrentUserId();
      Page<ForumThread> page = threadRepo.findByAuthorIdOrderByCreatedAtDesc(authorId, pageable);
      var threadListResponses = page.getContent().stream()
          .map(this::toListDto) // đã có sẵn trong class
          .toList();
      return PaginationResponse.from(new PageImpl<>(threadListResponses, pageable, page.getTotalElements()), pageable);
  }

  private ForumThreadResponse toDto(ForumThread thread) {
    var threadCategories = threadCatRepo.findByThread(thread);
    var categoryResponses = threadCategories.stream()
        .map(ForumThreadCategory::getCategory)
        .map(category -> new ForumCategoryResponse(
            category.getId(), category.getName(), category.getSlug(), category.getDescription(), category.getCreatedAt()))
        .toList();

    String authorName = null, authorAvatar = null;
    if (thread.getAuthorId() != null) {
      var user = userRepo.findById(thread.getAuthorId()).orElse(null);
      if (user != null) {
        authorName = user.getFullName();
        authorAvatar = user.getAvatarUrl();
      }
    }
    return new ForumThreadResponse(
        thread.getId(), thread.getAuthorId(), authorName, authorAvatar, thread.getTitle(), thread.getSlug(), thread.getBodyMd(),
        thread.isLocked(), thread.getViewCount(), thread.getReplyCount(),
        thread.getLastPostAt(), thread.getLastPostId(), thread.getLastPostAuthor(),
        thread.getCreatedAt(), thread.getUpdatedAt(), categoryResponses
    );
  }
  private ForumThreadListResponse toListDto(ForumThread thread) {
    var threadCategories = threadCatRepo.findByThread(thread);
    var categoryResponses = threadCategories.stream()
        .map(ForumThreadCategory::getCategory)
        .map(category -> new ForumCategoryResponse(
            category.getId(), category.getName(), category.getSlug(), category.getDescription(), category.getCreatedAt()))
        .toList();

    String authorName = null, authorAvatar = null;
    if (thread.getAuthorId() != null) {
      var user = userRepo.findById(thread.getAuthorId()).orElse(null);
      if (user != null) {
        authorName = user.getFullName();
        authorAvatar = user.getAvatarUrl();
      }
    }
    return new ForumThreadListResponse(
        thread.getId(), thread.getAuthorId(), authorName, authorAvatar, thread.getTitle(), thread.getSlug(),
        thread.isLocked(), thread.getViewCount(), thread.getReplyCount(),
        thread.getLastPostAt(), thread.getLastPostId(), thread.getLastPostAuthor(),
        thread.getCreatedAt(), thread.getUpdatedAt(), categoryResponses
    );
  }
}
