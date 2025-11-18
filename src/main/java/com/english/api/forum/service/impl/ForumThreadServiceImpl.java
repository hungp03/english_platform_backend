package com.english.api.forum.service.impl;

import com.english.api.auth.util.SecurityUtil;
import com.english.api.common.dto.PaginationResponse;
import com.english.api.forum.dto.request.ForumThreadCreateRequest;
import com.english.api.forum.dto.response.ForumCategoryResponse;
import com.english.api.forum.dto.response.ForumThreadResponse;
import com.english.api.forum.entity.ForumCategory;
import com.english.api.forum.entity.ForumThread;
import com.english.api.forum.entity.ForumThreadCategory;
import com.english.api.forum.repo.ForumCategoryRepository;
import com.english.api.forum.repo.ForumThreadCategoryRepository;
import com.english.api.forum.repo.ForumThreadRepository;
import com.english.api.user.repository.UserRepository;
import com.english.api.forum.service.ForumThreadService;
import com.english.api.forum.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import  com.english.api.forum.dto.response.ForumThreadListResponse;

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
    String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
    Page<ForumThread> page = threadRepo.search(kw, categoryId, locked, pageable);
    var mapped = page.getContent().stream().map(this::toListDto).toList();
    return PaginationResponse.from(new PageImpl<>(mapped, pageable, page.getTotalElements()), pageable);
  }

  @Override
  public ForumThreadResponse getBySlug(String slug) {
    var t = threadRepo.findBySlug(slug).orElseThrow();
    return toDto(t);
  }

  @Override
  @Async
  @Transactional
  public void increaseView(UUID threadId) {
    try {
      var t = threadRepo.findById(threadId).orElseThrow();
      t.setViewCount(t.getViewCount() + 1);
      threadRepo.save(t);
    } catch (Exception e) {
      // Log error but don't fail the main request
      // View count update is non-critical
      log.error("Failed to increase view count for thread {}: {}", threadId, e.getMessage(), e);
    }
  }

  @Override
  @Transactional
  public ForumThreadResponse create(ForumThreadCreateRequest req) {
    UUID currentUserId = SecurityUtil.getCurrentUserId();
    String base = (req.title() == null ? "" : req.title());
    String slug = SlugUtil.ensureUnique(SlugUtil.slugify(base),
        s -> threadRepo.findBySlug(s).isPresent());

    var t = ForumThread.builder()
        .authorId(currentUserId)
        .title(req.title())
        .slug(slug)
        .bodyMd(req.bodyMd())
        .locked(false)
        .viewCount(0)
        .replyCount(0)
        .lastPostAt(Instant.now())
        .build();
    t = threadRepo.save(t);

    if (req.categoryIds() != null && !req.categoryIds().isEmpty()) {
      List<ForumCategory> cats = categoryRepo.findAllById(req.categoryIds());
      for (ForumCategory c : cats) {
        threadCatRepo.save(ForumThreadCategory.builder().thread(t).category(c).build());
      }
    }
    return toDto(t);
  }

  @Override
  @Transactional
  public ForumThreadResponse adminLock(UUID id, boolean lock) {
    var t = threadRepo.findById(id).orElseThrow();
    t.setLocked(lock);
    t = threadRepo.save(t);
    return toDto(t);
  }

  @Override
  @Transactional
  public ForumThreadResponse lockByOwner(UUID id, boolean lock) {
    UUID currentUserId = SecurityUtil.getCurrentUserId();
    var t = threadRepo.findById(id).orElseThrow();
    if (t.getAuthorId() == null || !t.getAuthorId().equals(currentUserId)) {
      throw new org.springframework.security.access.AccessDeniedException("Only thread owner can lock/unlock this thread");
    }
    t.setLocked(lock);
    t = threadRepo.save(t);
    return toDto(t);
  }

  @Override
  @Transactional
  public void delete(UUID id) {
    threadRepo.deleteById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public PaginationResponse listByAuthor(UUID authorId, Pageable pageable) {
      // UUID uid = SecurityUtil.getCurrentUserId();
      Page<ForumThread> page = threadRepo.findByAuthorIdOrderByCreatedAtDesc(authorId, pageable);
      var mapped = page.getContent().stream()
          .map(this::toListDto) // đã có sẵn trong class
          .toList();
      return PaginationResponse.from(new PageImpl<>(mapped, pageable, page.getTotalElements()), pageable);
  }

  private ForumThreadResponse toDto(ForumThread t) {
    var tcs = threadCatRepo.findByThread(t);
    var cats = tcs.stream()
        .map(ForumThreadCategory::getCategory)
        .map(c -> new ForumCategoryResponse(
            c.getId(), c.getName(), c.getSlug(), c.getDescription(), c.getCreatedAt()))
        .toList();

    String tn=null, ta=null; if (t.getAuthorId()!=null){ var u=userRepo.findById(t.getAuthorId()).orElse(null); if(u!=null){ tn=u.getFullName(); ta=u.getAvatarUrl(); }}
    return new ForumThreadResponse(
        t.getId(), t.getAuthorId(), tn, ta, t.getTitle(), t.getSlug(), t.getBodyMd(),
        t.isLocked(), t.getViewCount(), t.getReplyCount(),
        t.getLastPostAt(), t.getLastPostId(), t.getLastPostAuthor(),
        t.getCreatedAt(), t.getUpdatedAt(), cats
    );
  }
  private ForumThreadListResponse toListDto(ForumThread t) {
    var tcs = threadCatRepo.findByThread(t);
    var cats = tcs.stream()
        .map(ForumThreadCategory::getCategory)
        .map(c -> new ForumCategoryResponse(
            c.getId(), c.getName(), c.getSlug(), c.getDescription(), c.getCreatedAt()))
        .toList();

    String tn=null, ta=null; if (t.getAuthorId()!=null){ var u=userRepo.findById(t.getAuthorId()).orElse(null); if(u!=null){ tn=u.getFullName(); ta=u.getAvatarUrl(); }}
    return new ForumThreadListResponse(
        t.getId(), t.getAuthorId(), tn, ta, t.getTitle(), t.getSlug(),
        t.isLocked(), t.getViewCount(), t.getReplyCount(),
        t.getLastPostAt(), t.getLastPostId(), t.getLastPostAuthor(),
        t.getCreatedAt(), t.getUpdatedAt(), cats
    );
  }
}
