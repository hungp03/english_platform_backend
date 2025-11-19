package com.english.api.forum.service.impl;

import com.english.api.auth.util.SecurityUtil;
import com.english.api.common.dto.PaginationResponse;
import com.english.api.common.exception.AccessDeniedException;
import com.english.api.forum.dto.request.ForumPostCreateRequest;
import com.english.api.forum.dto.response.ForumPostResponse;
import com.english.api.forum.entity.ForumPost;
import com.english.api.forum.mapper.ForumPostMapper;
import com.english.api.forum.repo.ForumPostRepository;
import com.english.api.forum.repo.ForumThreadRepository;
import com.english.api.forum.service.ForumPostService;
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
    private final ForumPostMapper forumPostMapper;

    @Override
    public PaginationResponse listByThread(UUID threadId, Pageable pageable, boolean onlyPublished) {
        var thread = threadRepo.findById(threadId).orElseThrow();

        Page<ForumPost> page = onlyPublished
                ? postRepo.findByThreadAndPublishedOrderByCreatedAtAsc(thread, true, pageable)
                : postRepo.findByThreadOrderByCreatedAtAsc(thread, pageable);

        var postsInPage = page.getContent();

        var postIdsInPage = postsInPage.stream()
                .map(ForumPost::getId)
                .collect(Collectors.toSet());

        var missingParentIds = postsInPage.stream()
                .map(ForumPost::getParent)
                .filter(Objects::nonNull)
                .map(ForumPost::getId)
                .filter(parentId -> !postIdsInPage.contains(parentId))
                .collect(Collectors.toSet());

        List<ForumPost> missingParents = missingParentIds.isEmpty()
                ? Collections.emptyList()
                : postRepo.findByIdIn(missingParentIds);

        missingParents.sort(Comparator.comparing(ForumPost::getCreatedAt));

        var allPostsWithParents = new ArrayList<ForumPost>(postsInPage.size() + missingParents.size());
        allPostsWithParents.addAll(postsInPage);
        allPostsWithParents.addAll(missingParents);

        var allAuthorIds = allPostsWithParents.stream()
                .map(ForumPost::getAuthorId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<UUID, User> userMap = allAuthorIds.isEmpty()
                ? Collections.emptyMap()
                : userRepo.findAllById(allAuthorIds).stream()
                .collect(Collectors.toMap(
                        User::getId,
                        u -> u
                ));

        var postResponsesInPage = postsInPage.stream()
                .map(post -> forumPostMapper.toResponse(post, userMap))
                .toList();

        var parentPostResponses = missingParents.stream()
                .map(post -> forumPostMapper.toResponse(post, userMap))
                .toList();

        var allPostResponses = new ArrayList<>(postResponsesInPage.size() + parentPostResponses.size());
        allPostResponses.addAll(postResponsesInPage);
        allPostResponses.addAll(parentPostResponses);

        Page<?> dtoPage = new PageImpl<>(
                allPostResponses,
                pageable,
                page.getTotalElements()
        );

        return PaginationResponse.from(dtoPage, pageable);
    }

    @Override
    @Transactional
    public ForumPostResponse create(UUID threadId, ForumPostCreateRequest request) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        var thread = threadRepo.findById(threadId).orElseThrow();

        if (thread.isLocked()) {
            throw new IllegalStateException("Thread is locked");
        }

        var post = ForumPost.builder()
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

        return forumPostMapper.toResponse(post);
    }

    @Override
    @Transactional
    public ForumPostResponse hide(UUID postId) {
        var post = postRepo.findById(postId).orElseThrow();
        post.setPublished(false);
        post = postRepo.save(post);
        return forumPostMapper.toResponse(post);
    }

    @Override
    @Transactional
    public ForumPostResponse show(UUID postId) {
        var post = postRepo.findById(postId).orElseThrow();
        post.setPublished(true);
        post = postRepo.save(post);
        return forumPostMapper.toResponse(post);
    }

    @Override
    @Transactional
    public void deleteByOwner(UUID postId) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        var post = postRepo.findById(postId).orElseThrow();
        if (post.getAuthorId() == null || !post.getAuthorId().equals(currentUserId)) {
            throw new AccessDeniedException("Only author can delete this post");
        }
        // adjust thread counters
        var thread = post.getThread();

        // nếu là post cấp 1 (không có parent) -> xoá hết con rồi xoá nó
        if (post.getParent() == null) {
            postRepo.deleteByParent(post);
        }
        postRepo.delete(post);

        if (thread != null) {
            long replyCount = Math.max(0, thread.getReplyCount() - 1);
            thread.setReplyCount(replyCount);
            threadRepo.save(thread);
        }
    }

    @Override
    @Transactional
    public void adminDelete(UUID postId) {
        var post = postRepo.findById(postId).orElseThrow();
        var thread = post.getThread();
        postRepo.delete(post);
        if (thread != null) {
            long replyCount = Math.max(0, thread.getReplyCount() - 1);
            thread.setReplyCount(replyCount);
            threadRepo.save(thread);
        }
    }
}
