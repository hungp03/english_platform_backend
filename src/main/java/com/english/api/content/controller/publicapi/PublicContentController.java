package com.english.api.content.controller.publicapi;

import com.english.api.content.dto.request.PostFilterRequest;
import com.english.api.content.dto.response.CommentResponse;
import com.english.api.content.dto.response.PublicPostDetailResponse;
import com.english.api.content.dto.response.PublicPostSummaryResponse;
import com.english.api.content.service.ContentCommentService;
import com.english.api.content.service.ContentPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/content")
@RequiredArgsConstructor
public class PublicContentController {

    private final ContentPostService postService;
    private final ContentCommentService commentService;

    @GetMapping("/posts")
    public ResponseEntity<Page<PublicPostSummaryResponse>> list(PostFilterRequest filter, Pageable pageable) {
        return ResponseEntity.ok(postService.publicList(filter, pageable));
    }

    @GetMapping("/posts/{slug}")
    public ResponseEntity<PublicPostDetailResponse> detail(@PathVariable String slug) {
        return ResponseEntity.ok(postService.publicDetailBySlug(slug));
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<Page<CommentResponse>> comments(@PathVariable java.util.UUID postId, Pageable pageable) {
        return ResponseEntity.ok(commentService.listByPost(postId, pageable, false));
    }
}