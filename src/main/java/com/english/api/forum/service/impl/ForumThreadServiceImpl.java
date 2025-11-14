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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.english.api.forum.dto.response.ForumThreadListResponse;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
    
    // Phase 1: Get paginated thread IDs
    Page<ForumThread> page = threadRepo.search(kw, categoryId, locked, pageable);
    
    if (page.isEmpty()) {
      return PaginationResponse.from(page.map(this::toListDto), pageable);
    }
    
    // Phase 2: Extract IDs and batch fetch all associations
    List<UUID> threadIds = page.getContent().stream()
        .map(ForumThread::getId)
        .toList();
    
    // Batch fetch threads (already loaded, just preserving order)
    List<ForumThread> threads = threadRepo.findByIdInOrderPreserved(threadIds);
    
    // Batch fetch thread categories with categories
    List<ForumThreadCategory> allThreadCategories = threadCatRepo.findByThreadIdsWithCategory(threadIds);
    
    // Batch fetch unique authors
    List<UUID> authorIds = page.getContent().stream()
        .map(ForumThread::getAuthorId)
        .filter(id -> id != null)
        .distinct()
        .toList();
    
    Map<UUID, com.english.api.user.model.User> authorMap = authorIds.isEmpty() 
        ? Collections.emptyMap()
        : userRepo.findByIdIn(authorIds).stream()
            .collect(Collectors.toMap(
                com.english.api.user.model.User::getId, 
                u -> u
            ));
    
    // Group categories by thread ID
    Map<UUID, List<ForumThreadCategory>> categoriesByThread = allThreadCategories.stream()
        .collect(Collectors.groupingBy(tc -> tc.getThread().getId()));
    
    // Preserve pagination order and map to DTOs
    Map<UUID, ForumThread> threadMap = threads.stream()
        .collect(Collectors.toMap(ForumThread::getId, t -> t));
    
    List<ForumThreadListResponse> responses = threadIds.stream()
        .map(id -> {
          ForumThread t = threadMap.get(id);
          List<ForumThreadCategory> tcs = categoriesByThread.getOrDefault(id, Collections.emptyList());
          com.english.api.user.model.User author = t.getAuthorId() != null ? authorMap.get(t.getAuthorId()) : null;
          return toListDtoWithPreloadedData(t, tcs, author);
        })
        .toList();
    
    Page<ForumThreadListResponse> responsePage = new PageImpl<>(
        responses, pageable, page.getTotalElements()
    );
    
    return PaginationResponse.from(responsePage, pageable);
  }

  @Override
  public ForumThreadResponse getBySlug(String slug) {
    var t = threadRepo.findBySlug(slug).orElseThrow();
    return toDto(t);
  }

  @Override
  @Transactional
  public void increaseView(UUID threadId) {
    var t = threadRepo.findById(threadId).orElseThrow();
    t.setViewCount(t.getViewCount() + 1);
    threadRepo.save(t);
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
      throw new org.springframework.security.access.AccessDeniedException(
          "Only thread owner can lock/unlock this thread");
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
    // Phase 1: Get paginated thread IDs
    Page<ForumThread> page = threadRepo.findByAuthorIdOrderByCreatedAtDesc(authorId, pageable);
    
    if (page.isEmpty()) {
      return PaginationResponse.from(page.map(this::toListDto), pageable);
    }
    
    // Phase 2: Extract IDs and batch fetch all associations
    List<UUID> threadIds = page.getContent().stream()
        .map(ForumThread::getId)
        .toList();
    
    // Batch fetch threads
    List<ForumThread> threads = threadRepo.findByIdInOrderPreserved(threadIds);
    
    // Batch fetch thread categories with categories
    List<ForumThreadCategory> allThreadCategories = threadCatRepo.findByThreadIdsWithCategory(threadIds);
    
    // Fetch author once
    com.english.api.user.model.User author = userRepo.findById(authorId).orElse(null);
    
    // Group categories by thread ID
    Map<UUID, List<ForumThreadCategory>> categoriesByThread = allThreadCategories.stream()
        .collect(Collectors.groupingBy(tc -> tc.getThread().getId()));
    
    // Preserve pagination order and map to DTOs
    Map<UUID, ForumThread> threadMap = threads.stream()
        .collect(Collectors.toMap(ForumThread::getId, t -> t));
    
    List<ForumThreadListResponse> responses = threadIds.stream()
        .map(id -> {
          ForumThread t = threadMap.get(id);
          List<ForumThreadCategory> tcs = categoriesByThread.getOrDefault(id, Collections.emptyList());
          return toListDtoWithPreloadedData(t, tcs, author);
        })
        .toList();
    
    Page<ForumThreadListResponse> responsePage = new PageImpl<>(
        responses, pageable, page.getTotalElements()
    );
    
    return PaginationResponse.from(responsePage, pageable);
  }

  private ForumThreadResponse toDto(ForumThread t) {
    var tcs = threadCatRepo.findByThread(t);
    var cats = tcs.stream()
        .map(ForumThreadCategory::getCategory)
        .map(c -> new ForumCategoryResponse(
            c.getId(), c.getName(), c.getSlug(), c.getDescription(), c.getCreatedAt()))
        .toList();

    String tn = null, ta = null;
    if (t.getAuthorId() != null) {
      var u = userRepo.findById(t.getAuthorId()).orElse(null);
      if (u != null) {
        tn = u.getFullName();
        ta = u.getAvatarUrl();
      }
    }
    return new ForumThreadResponse(
        t.getId(), t.getAuthorId(), tn, ta, t.getTitle(), t.getSlug(), t.getBodyMd(),
        t.isLocked(), t.getViewCount(), t.getReplyCount(),
        t.getLastPostAt(), t.getLastPostId(), t.getLastPostAuthor(),
        t.getCreatedAt(), t.getUpdatedAt(), cats);
  }

  private ForumThreadListResponse toListDto(ForumThread t) {
    var tcs = threadCatRepo.findByThread(t);
    var cats = tcs.stream()
        .map(ForumThreadCategory::getCategory)
        .map(c -> new ForumCategoryResponse(
            c.getId(), c.getName(), c.getSlug(), c.getDescription(), c.getCreatedAt()))
        .toList();

    String tn = null, ta = null;
    if (t.getAuthorId() != null) {
      var u = userRepo.findById(t.getAuthorId()).orElse(null);
      if (u != null) {
        tn = u.getFullName();
        ta = u.getAvatarUrl();
      }
    }
    return new ForumThreadListResponse(
        t.getId(), t.getAuthorId(), tn, ta, t.getTitle(), t.getSlug(),
        t.isLocked(), t.getViewCount(), t.getReplyCount(),
        t.getLastPostAt(), t.getLastPostId(), t.getLastPostAuthor(),
        t.getCreatedAt(), t.getUpdatedAt(), cats);
  }
  
  private ForumThreadListResponse toListDtoWithPreloadedData(
      ForumThread t, 
      List<ForumThreadCategory> tcs, 
      com.english.api.user.model.User author) {
    var cats = tcs.stream()
        .map(ForumThreadCategory::getCategory)
        .map(c -> new ForumCategoryResponse(
            c.getId(), c.getName(), c.getSlug(), c.getDescription(), c.getCreatedAt()))
        .toList();

    String tn = null, ta = null;
    if (author != null) {
      tn = author.getFullName();
      ta = author.getAvatarUrl();
    }
    
    return new ForumThreadListResponse(
        t.getId(), t.getAuthorId(), tn, ta, t.getTitle(), t.getSlug(),
        t.isLocked(), t.getViewCount(), t.getReplyCount(),
        t.getLastPostAt(), t.getLastPostId(), t.getLastPostAuthor(),
        t.getCreatedAt(), t.getUpdatedAt(), cats);
  }
}
