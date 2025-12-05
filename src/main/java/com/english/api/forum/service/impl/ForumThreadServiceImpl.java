package com.english.api.forum.service.impl;

import com.english.api.auth.util.SecurityUtil;
import com.english.api.common.dto.PaginationResponse;
import com.english.api.common.exception.AccessDeniedException;
import com.english.api.common.exception.ResourceInvalidException;
import com.english.api.common.exception.ResourceNotFoundException;
import com.english.api.forum.dto.request.ForumThreadCreateRequest;
import com.english.api.forum.dto.request.ForumThreadUpdateRequest;
import com.english.api.forum.dto.response.ForumCategoryResponse;
import com.english.api.forum.dto.response.ForumThreadListResponse;
import com.english.api.forum.dto.response.ForumThreadResponse;
import com.english.api.forum.model.ForumCategory;
import com.english.api.forum.model.ForumThread;
import com.english.api.forum.model.ForumThreadCategory;
import com.english.api.forum.model.ForumThreadSave;
import com.english.api.forum.repository.*;
import com.english.api.forum.service.ForumThreadService;
import com.english.api.forum.util.SlugUtil;
import com.english.api.user.model.User;
import com.english.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.english.api.notification.service.NotificationService;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ForumThreadServiceImpl implements ForumThreadService {

    private final ForumThreadRepository threadRepo;
    private final UserRepository userRepo;
    private final ForumCategoryRepository categoryRepo;
    private final ForumThreadCategoryRepository threadCatRepo;
    private final ForumPostRepository postRepo;
    private final NotificationService notificationService;
    private final ForumReportRepository reportRepo;
    private final ForumThreadSaveRepository saveRepo;

    @Override
    public PaginationResponse listPublic(String keyword, UUID categoryId, Boolean locked, Pageable pageable) {
        String normalizedKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        Page<ForumThread> page = threadRepo.search(normalizedKeyword, categoryId, locked, pageable);
        List<ForumThreadListResponse> threadListResponses = page.getContent().stream().map(this::toListDto).toList();
        return PaginationResponse.from(new PageImpl<>(threadListResponses, pageable, page.getTotalElements()), pageable);
    }

    @Override
    public ForumThreadResponse getBySlug(String slug) {
        ForumThread thread = threadRepo.findBySlug(slug).orElseThrow(() -> new ResourceNotFoundException("Thread not found"));
        return toDto(thread);
    }

    @Override
    @Async
    @Transactional
    public void increaseView(UUID threadId) {
        try {
            ForumThread thread = threadRepo.findById(threadId).orElseThrow();
            thread.setViewCount(thread.getViewCount() + 1);
            threadRepo.save(thread);
        } catch (Exception e) {
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

        ForumThread thread = ForumThread.builder()
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
        ForumThread thread = threadRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Thread not found"));
        thread.setLocked(lock);
        thread = threadRepo.save(thread);
        return toDto(thread);
    }

    @Override
    @Transactional
    public ForumThreadResponse lockByOwner(UUID id, boolean lock) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        ForumThread thread = threadRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Thread not found"));
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
        // Lưu ý: Method này ít dùng, thường dùng adminDelete hoặc deleteByOwner để clean data
        saveRepo.deleteByThreadId(id); // Xóa saves trước
        threadRepo.deleteById(id);
    }

    // @Override
    // @Transactional(readOnly = true)
    // public PaginationResponse listByAuthor(UUID authorId, Pageable pageable) {
    //     Page<ForumThread> page = threadRepo.findByAuthorIdOrderByCreatedAtDesc(authorId, pageable);
    //     List<ForumThreadListResponse> threadListResponses = page.getContent().stream()
    //             .map(this::toListDto)
    //             .toList();
    //     return PaginationResponse.from(new PageImpl<>(threadListResponses, pageable, page.getTotalElements()), pageable);
    // }
    @Override
    @Transactional(readOnly = true)
    public PaginationResponse listByAuthor(UUID authorId, String keyword, UUID categoryId, Boolean locked, Pageable pageable) {
        String normalizedKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        
        // Gọi query mới searchByAuthor thay vì findByAuthorIdOrderByCreatedAtDesc
        Page<ForumThread> page = threadRepo.searchByAuthor(authorId, normalizedKeyword, categoryId, locked, pageable);
        

        List<ForumThreadListResponse> threadListResponses = page.getContent().stream()
                .map(this::toListDto)
                .toList();
                
        return PaginationResponse.from(new PageImpl<>(threadListResponses, pageable, page.getTotalElements()), pageable);
    }

    @Override
    @Transactional
    public ForumThreadResponse updateByOwner(UUID id, ForumThreadUpdateRequest req) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        ForumThread thread = threadRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Thread not found"));

        if (thread.getAuthorId() == null || !thread.getAuthorId().equals(currentUserId)) {
            throw new AccessDeniedException("Bạn không có quyền sửa bài viết này");
        }

        if (thread.isLocked()) {
            throw new ResourceInvalidException("Bài viết đang bị khóa, không thể chỉnh sửa");
        }

        if (req.title() != null && !req.title().isBlank() && !req.title().equals(thread.getTitle())) {
            thread.setTitle(req.title());
            String newSlug = SlugUtil.ensureUnique(SlugUtil.slugify(req.title()),
                    s -> threadRepo.findBySlug(s).isPresent());
            thread.setSlug(newSlug);
        }

        if (req.bodyMd() != null) {
            thread.setBodyMd(req.bodyMd());
        }

        if (req.categoryIds() != null) {
            List<ForumThreadCategory> oldLinks = threadCatRepo.findByThread(thread);
            threadCatRepo.deleteAll(oldLinks);

            if (!req.categoryIds().isEmpty()) {
                List<ForumCategory> categories = categoryRepo.findAllById(req.categoryIds());
                for (ForumCategory category : categories) {
                    threadCatRepo.save(ForumThreadCategory.builder()
                            .thread(thread)
                            .category(category)
                            .build());
                }
            }
        }

        thread = threadRepo.save(thread);
        return toDto(thread);
    }

    @Override
    @Transactional
    public void deleteByOwner(UUID id) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        ForumThread thread = threadRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Thread not found"));

        if (thread.getAuthorId() == null || !thread.getAuthorId().equals(currentUserId)) {
            throw new AccessDeniedException("Bạn không có quyền xóa bài viết này");
        }

        performDeleteThread(thread);
    }

    @Override
    @Transactional
    public void adminDelete(UUID id) {
        ForumThread thread = threadRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Thread not found"));

        UUID adminId = SecurityUtil.getCurrentUserId();
        User adminUser = userRepo.findById(adminId).orElse(null);
        String adminName = adminUser != null ? adminUser.getFullName() : "Quản trị viên";

        UUID threadAuthorId = thread.getAuthorId();
        String threadTitle = thread.getTitle() != null ? thread.getTitle() : "";

        performDeleteThread(thread);

        if (threadAuthorId != null && !threadAuthorId.equals(adminId)) {
            String title = "Chủ đề của bạn đã bị xóa bởi quản trị";
            String body = adminName + " đã xóa chủ đề của bạn";
            if (!threadTitle.isBlank()) {
                body += ": \"" + threadTitle + "\".";
            } else {
                body += ".";
            }
            notificationService.sendNotification(threadAuthorId, title, body);
        }
    }

    // --- CÁC PHƯƠNG THỨC MỚI CHO TÍNH NĂNG SAVE/YÊU THÍCH ---

    @Override
    @Transactional
    public void toggleSaveThread(UUID threadId) {
        UUID userId = SecurityUtil.getCurrentUserId();
        ForumThread thread = threadRepo.findById(threadId)
                .orElseThrow(() -> new ResourceNotFoundException("Thread not found"));

        Optional<ForumThreadSave> existingSave = saveRepo.findByUserIdAndThreadId(userId, threadId);

        if (existingSave.isPresent()) {
            saveRepo.delete(existingSave.get());
        } else {
            User user = userRepo.getReferenceById(userId);
            ForumThreadSave save = ForumThreadSave.builder()
                    .user(user)
                    .thread(thread)
                    .build();
            saveRepo.save(save);
        }
    }

    // @Override
    // @Transactional(readOnly = true)
    // public PaginationResponse listSavedThreads(String keyword, UUID categoryId, Pageable pageable) {
    //     UUID userId = SecurityUtil.getCurrentUserId();
        
    //     // FIX QUAN TRỌNG: Truyền chuỗi rỗng "" nếu keyword là null để tránh lỗi Postgres lower(bytea)
    //     String searchKeyword = (keyword != null && !keyword.isBlank()) ? keyword.trim() : "";

    //     // Gọi hàm repository với chuỗi rỗng (LIKE '%%' sẽ lấy tất cả)
    //     Page<ForumThreadSave> savesPage = saveRepo.searchSavedThreads(userId, searchKeyword, categoryId, pageable);

    //     List<ForumThreadListResponse> responses = savesPage.getContent().stream()
    //             .map(ForumThreadSave::getThread)
    //             .map(this::toListDto) 
    //             .toList();

    //     return PaginationResponse.from(new PageImpl<>(responses, pageable, savesPage.getTotalElements()), pageable);
    // }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse listSavedThreads(String keyword, UUID categoryId, Boolean locked, Pageable pageable) {
        UUID userId = SecurityUtil.getCurrentUserId();
        String normalizedKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();

        // Thêm tham số locked vào query
        Page<ForumThreadSave> savesPage = saveRepo.searchSavedThreads(userId, normalizedKeyword, categoryId, locked, pageable);

        List<ForumThreadListResponse> responses = savesPage.getContent().stream()
                        .map(ForumThreadSave::getThread)
                        .map(this::toListDto) // <--- SỬA LỖI Ở ĐÂY: Gọi method reference hoặc lambda 1 tham số
                        .toList();

        return PaginationResponse.from(new PageImpl<>(responses, pageable, savesPage.getTotalElements()), pageable);
    }

    // --- HELPERS ---

    private void performDeleteThread(ForumThread thread) {
        // 1. Xóa tất cả lượt lưu/yêu thích của thread này trước
        saveRepo.deleteByThreadId(thread.getId());

        // 2. Xóa các liên kết khác
        threadCatRepo.deleteByThread(thread);
        postRepo.unlinkParentsByThread(thread);
        postRepo.deleteAllByThread(thread);

        // 3. Xóa reports
        List<UUID> postIds = postRepo.findIdsByThread(thread);
        if (!postIds.isEmpty()) {
            reportRepo.deleteByTargetIds(postIds);
        }
        reportRepo.deleteByTargetId(thread.getId());

        // 4. Xóa thread
        threadRepo.delete(thread);
    }

    // Helper để lấy currentUserId mà không ném lỗi nếu là anonymous
    private UUID getCurrentUserIdSafe() {
        try {
            return SecurityUtil.getCurrentUserId();
        } catch (Exception e) {
            return null;
        }
    }

    // Giữ nguyên signature như yêu cầu
    private ForumThreadResponse toDto(ForumThread thread) {
        // Tự động lấy current user ID bên trong
        UUID currentUserId = getCurrentUserIdSafe();

        List<ForumThreadCategory> threadCategories = threadCatRepo.findByThread(thread);
        List<ForumCategoryResponse> categoryResponses = threadCategories.stream()
                .map(ForumThreadCategory::getCategory)
                .map(category -> new ForumCategoryResponse(
                        category.getId(), category.getName(), category.getSlug(), category.getDescription(), category.getCreatedAt()))
                .toList();

        String authorName = null, authorAvatar = null;
        if (thread.getAuthorId() != null) {
            User user = userRepo.findById(thread.getAuthorId()).orElse(null);
            if (user != null) {
                authorName = user.getFullName();
                authorAvatar = user.getAvatarUrl();
            }
        }

        // Check saved status
        boolean isSaved = false;
        if (currentUserId != null) {
            isSaved = saveRepo.existsByUserIdAndThreadId(currentUserId, thread.getId());
        }

        return new ForumThreadResponse(
                thread.getId(), thread.getAuthorId(), authorName, authorAvatar, thread.getTitle(), thread.getSlug(), thread.getBodyMd(),
                thread.isLocked(), thread.getViewCount(), thread.getReplyCount(),
                thread.getLastPostAt(), thread.getLastPostId(), thread.getLastPostAuthor(),
                thread.getCreatedAt(), thread.getUpdatedAt(), categoryResponses,
                isSaved // Trường mới
        );
    }

    // Giữ nguyên signature như yêu cầu
    private ForumThreadListResponse toListDto(ForumThread thread) {
        // Tự động lấy current user ID bên trong
        UUID currentUserId = getCurrentUserIdSafe();

        List<ForumThreadCategory> threadCategories = threadCatRepo.findByThread(thread);
        List<ForumCategoryResponse> categoryResponses = threadCategories.stream()
                .map(ForumThreadCategory::getCategory)
                .map(category -> new ForumCategoryResponse(
                        category.getId(), category.getName(), category.getSlug(), category.getDescription(), category.getCreatedAt()))
                .toList();

        String authorName = null, authorAvatar = null;
        if (thread.getAuthorId() != null) {
            User user = userRepo.findById(thread.getAuthorId()).orElse(null);
            if (user != null) {
                authorName = user.getFullName();
                authorAvatar = user.getAvatarUrl();
            }
        }

        // Check saved status
        boolean isSaved = false;
        if (currentUserId != null) {
            isSaved = saveRepo.existsByUserIdAndThreadId(currentUserId, thread.getId());
        }

        return new ForumThreadListResponse(
                thread.getId(), thread.getAuthorId(), authorName, authorAvatar, thread.getTitle(), thread.getSlug(),
                thread.isLocked(), thread.getViewCount(), thread.getReplyCount(),
                thread.getLastPostAt(), thread.getLastPostId(), thread.getLastPostAuthor(),
                thread.getCreatedAt(), thread.getUpdatedAt(), categoryResponses,
                isSaved // Trường mới
        );
    }
}