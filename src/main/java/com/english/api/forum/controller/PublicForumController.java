package com.english.api.forum.controller;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.forum.dto.response.ForumCategoryResponse;
import com.english.api.forum.dto.response.ForumThreadResponse;
import com.english.api.forum.service.ForumCategoryService;
import com.english.api.forum.service.ForumPostService;
import com.english.api.forum.service.ForumThreadService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Sort;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public/forum")
public class PublicForumController {

    private final ForumThreadService threadService;
    private final ForumPostService postService;
    private final ForumCategoryService categoryService;

    @GetMapping("/threads")
    public ResponseEntity<PaginationResponse> listThreads(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) Boolean locked,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize
    ) {
        Sort sort = sortDirection.equalsIgnoreCase("asc") 
            ? Sort.by(sortBy).ascending() 
            : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), pageSize, sort);
        return ResponseEntity.ok(threadService.listPublic(keyword, categoryId, locked, pageable));
    }


    @GetMapping("/threads/{slug}")
    public ResponseEntity<ForumThreadResponse> threadDetail(@PathVariable String slug) {
        ForumThreadResponse forumThreadResponse = threadService.getBySlug(slug);
        threadService.increaseView(forumThreadResponse.id());
        return ResponseEntity.ok(forumThreadResponse);
    }

    @GetMapping("/threads/{threadId}/posts")
    public ResponseEntity<PaginationResponse> listPosts(
            @PathVariable UUID threadId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize
    ) {
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), pageSize);
        return ResponseEntity.ok(postService.listByThread(threadId, pageable, true));
    }


    @GetMapping("/categories")
    public ResponseEntity<java.util.List<ForumCategoryResponse>> categories() {
        return ResponseEntity.ok(categoryService.list());
    }
}
