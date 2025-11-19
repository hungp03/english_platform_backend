package com.english.api.blog.controller.publicapi;

import com.english.api.blog.dto.request.PostFilterRequest;
import com.english.api.blog.dto.response.BlogCategoryResponse;
import com.english.api.blog.dto.response.PublicPostDetailResponse;
import com.english.api.blog.service.BlogCategoryService;
import com.english.api.blog.service.BlogCommentService;
import com.english.api.blog.service.BlogPostService;
import com.english.api.common.dto.PaginationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/public/content")
@RequiredArgsConstructor
public class PublicBlogController {

    private final BlogPostService postService;
    private final BlogCommentService commentService;
    private final BlogCategoryService categoryService;

    @GetMapping("/posts")
    public ResponseEntity<PaginationResponse> list(@ModelAttribute PostFilterRequest filter, Pageable pageable) {
        return ResponseEntity.ok(postService.publicList(filter, pageable));
    }

    @GetMapping("/posts/{slug}")
    public ResponseEntity<PublicPostDetailResponse> detail(@PathVariable String slug) {
        return ResponseEntity.ok(postService.publicDetailBySlug(slug));
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<PaginationResponse> comments(@PathVariable UUID postId, Pageable pageable) {
        return ResponseEntity.ok(commentService.listByPost(postId, pageable, false));
    }

    @GetMapping("/categories")
    public ResponseEntity<PaginationResponse> list(Pageable pageable) {
        return ResponseEntity.ok(categoryService.list(pageable));
    }
}
