package com.english.api.forum.service.impl;

import com.english.api.auth.util.SecurityUtil;
import com.english.api.common.dto.PaginationResponse;
import com.english.api.common.exception.AccessDeniedException;
import com.english.api.common.exception.ResourceInvalidException;
import com.english.api.common.exception.ResourceNotFoundException;
import com.english.api.forum.dto.request.ForumPostCreateRequest;
import com.english.api.forum.dto.response.ForumPostResponse;
import com.english.api.forum.model.ForumPost;
import com.english.api.forum.model.ForumThread;
import com.english.api.forum.repository.ForumPostRepository;
import com.english.api.forum.repository.ForumReportRepository;
import com.english.api.forum.repository.ForumThreadRepository;
import com.english.api.forum.service.ForumPostService;
import com.english.api.notification.service.NotificationService;
import com.english.api.user.model.User;
import com.english.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ForumPostServiceImpl implements ForumPostService {

    private final ForumPostRepository postRepo;
    private final ForumThreadRepository threadRepo;
    private final UserRepository userRepo;
    private final NotificationService notificationService;
    private final ForumReportRepository reportRepo;

    @Override
    public PaginationResponse listByThread(UUID threadId, Pageable pageable, boolean onlyPublished) {
        ForumThread thread = threadRepo.findById(threadId).orElseThrow();

        // 1. Lấy page bình thường như cũ
        Page<ForumPost> page = onlyPublished
                ? postRepo.findByThreadAndPublishedOrderByCreatedAtAsc(thread, true, pageable)
                : postRepo.findByThreadOrderByCreatedAtAsc(thread, pageable);

        List<ForumPost> postsInPage = page.getContent();

        // 2. Lấy id các post trong page
        Set<UUID> pageIds = postsInPage.stream()
                .map(ForumPost::getId)
                .collect(Collectors.toSet());

        // 3. Tìm các parentId của post trong page, loại null, loại những thằng đã có trong page
        Set<UUID> missingParentIds = postsInPage.stream()
                .map(ForumPost::getParent)
                .filter(Objects::nonNull)
                .map(ForumPost::getId)
                .filter(parentId -> !pageIds.contains(parentId))
                .collect(Collectors.toSet());

        // 4. Query các parent bị thiếu
        List<ForumPost> missingParents = new ArrayList<>(missingParentIds.isEmpty()
                ? Collections.emptyList()
                : postRepo.findByIdIn(missingParentIds));

        // Optional: sort parent cho đẹp
        missingParents.sort(Comparator.comparing(ForumPost::getCreatedAt));

        // 5. Map DTO: post trong page (chính)
        List<ForumPostResponse> mainDtos = postsInPage.stream()
                .map(this::toDto)
                .toList();

        // 6. Map DTO: parent bổ sung
        List<ForumPostResponse> parentDtos = missingParents.stream()
                .map(this::toDto)
                .toList();

        // 7. Gộp lại: result = [20 post trong page] + [các parent bổ sung]
        ArrayList<ForumPostResponse> combined = new ArrayList<>(mainDtos.size() + parentDtos.size());
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
    public ForumPostResponse create(UUID threadId, ForumPostCreateRequest request) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        ForumThread thread = threadRepo.findById(threadId).orElseThrow();

        if (thread.isLocked()) {
            throw new ResourceInvalidException("Thread is locked");
        }

        ForumPost post = ForumPost.builder()
                .thread(thread)
                .parent(request.parentId() == null ? null : ForumPost.builder().id(request.parentId()).build())
                .authorId(currentUserId)
                .bodyMd(request.bodyMd())
                .published(true)
                .build();

        post = postRepo.save(post);

        thread.setReplyCount(thread.getReplyCount() + 1);
        thread.setLastPostAt(Instant.now());
        thread.setLastPostId(post.getId());
        thread.setLastPostAuthor(currentUserId);
        threadRepo.save(thread);


        // Xác định parent post nếu có
        ForumPost parentPost = null;
        if (request.parentId() != null) {
            parentPost = postRepo.findById(request.parentId()).orElse(null);
        }
        // Lấy thông tin người đang comment để hiển thị tên trong thông báo
        User currentUser = userRepo.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        // 1. Gửi thông báo cho chủ thread (nếu người comment không phải chủ thread)
        if (thread.getAuthorId() != null && !thread.getAuthorId().equals(currentUserId)) {
            notificationService.sendNotification(
                    thread.getAuthorId(),
                    "Phản hồi mới trong chủ đề của bạn",
                    currentUser.getFullName() + " đã bình luận trong chủ đề của bạn: \"" + thread.getTitle() + "\""
            );
        }

        // 2. Gửi thông báo cho chủ comment cha (nếu là reply và người reply không phải chủ comment cha)
        if (parentPost != null && parentPost.getAuthorId() != null
            && !parentPost.getAuthorId().equals(currentUserId)) {
            // Tránh gửi trùng nếu chủ thread cũng là chủ comment cha
            if (!parentPost.getAuthorId().equals(thread.getAuthorId())) {
                notificationService.sendNotification(
                        parentPost.getAuthorId(),
                        "Phản hồi mới trong bình luận của bạn",
                        currentUser.getFullName() + " đã phản hồi trong bài post của bạn: \"" + thread.getTitle() + "\""
                );
            }
        }
        return toDto(post);
    }

    @Override
    @Transactional
    public ForumPostResponse hide(UUID postId) {
        ForumPost post = postRepo.findById(postId).orElseThrow();
        post.setPublished(false);
        post = postRepo.save(post);
        return toDto(post);
    }

    @Override
    @Transactional
    public ForumPostResponse show(UUID postId) {
        ForumPost post = postRepo.findById(postId).orElseThrow();
        post.setPublished(true);
        post = postRepo.save(post);
        return toDto(post);
    }

    private ForumPostResponse toDto(ForumPost post) {
        String authorName = null;
        String authorAvatarUrl = null;

        if (post.getAuthorId() != null) {
            User user = userRepo.findById(post.getAuthorId()).orElse(null);
            if (user != null) {
                authorName = user.getFullName();
                authorAvatarUrl = user.getAvatarUrl();
            }
        }

        return new ForumPostResponse(
                post.getId(),
                post.getThread() != null ? post.getThread().getId() : null,
                post.getParent() != null ? post.getParent().getId() : null,
                post.getAuthorId(),
                authorName,
                authorAvatarUrl,
                post.getBodyMd(),
                post.isPublished(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }


    @Override
    @Transactional
    public void deleteByOwner(UUID postId) {
        UUID uid = SecurityUtil.getCurrentUserId();
        ForumPost post = postRepo.findById(postId).orElseThrow();

        if (post.getAuthorId() == null || !post.getAuthorId().equals(uid)) {
            throw new AccessDeniedException("Only author can delete this post");
        }

        performDelete(post);
    }

    @Override
    @Transactional
    public void adminDelete(UUID postId) {
        ForumPost post = postRepo.findById(postId).orElseThrow();

        UUID adminId = SecurityUtil.getCurrentUserId();
        User adminUser = userRepo.findById(adminId).orElse(null);
        String adminName = adminUser != null ? adminUser.getFullName() : "Quản trị viên";

        UUID postAuthorId = post.getAuthorId();
        UUID threadAuthorId = post.getThread() != null ? post.getThread().getAuthorId() : null;
        String threadTitle = post.getThread() != null ? post.getThread().getTitle() : "";

        performDelete(post);

        // Gửi thông báo tới chủ post (nếu có và khác admin)
        if (postAuthorId != null && !postAuthorId.equals(adminId)) {
            String title = "Bài viết của bạn đã bị xóa";
            String body = adminName + " đã xóa bài viết của bạn";
            if (threadTitle != null && !threadTitle.isBlank()) {
                body += " trong chủ đề: \"" + threadTitle + "\".";
            } else {
                body += ".";
            }
            notificationService.sendNotification(postAuthorId, title, body);
        }

        if (threadAuthorId != null && !threadAuthorId.equals(adminId)
            && (postAuthorId == null || !threadAuthorId.equals(postAuthorId))) {
            String title = "Bài viết trong chủ đề của bạn đã bị xóa";
            String body = adminName + " đã xóa một bài viết trong chủ đề của bạn";
            if (threadTitle != null && !threadTitle.isBlank()) {
                body += ": \"" + threadTitle + "\".";
            } else {
                body += ".";
            }
            notificationService.sendNotification(threadAuthorId, title, body);
        }
    }

    private void performDelete(ForumPost post) {
        ForumThread thread = post.getThread();
        long deletedCount = 1;

        // 1. Xóa các bài con (nếu có)
        if (post.getParent() == null) {
            long childrenCount = postRepo.countByParent(post);
            if (childrenCount > 0) {

                List<UUID> childIds = postRepo.findIdsByParent(post);
                if (!childIds.isEmpty()) {
                    reportRepo.deleteByTargetIds(childIds);
                }

                postRepo.deleteByParent(post);
                deletedCount += childrenCount;
            }
        }

        postRepo.delete(post);

        //Flush để đảm bảo lệnh xóa đã được ghi nhận trước khi ta query tìm bài mới nhất
        postRepo.flush();

        if (thread != null) {
            long currentCount = thread.getReplyCount();
            long newCount = Math.max(0, currentCount - deletedCount);
            thread.setReplyCount(newCount);

            Optional<ForumPost> latestPostOpt = postRepo.findFirstByThreadOrderByCreatedAtDesc(thread);

            if (latestPostOpt.isPresent()) {
                ForumPost latestPost = latestPostOpt.get();
                thread.setLastPostAt(latestPost.getCreatedAt());
                thread.setLastPostId(latestPost.getId());
                thread.setLastPostAuthor(latestPost.getAuthorId());
            } else {
                thread.setLastPostAt(thread.getCreatedAt());
                thread.setLastPostId(null);
                thread.setLastPostAuthor(null);
            }

            threadRepo.save(thread);
        }
    }
}
