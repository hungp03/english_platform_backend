package com.english.api.content.controller.admin;

import com.english.api.common.dto.PaginationResponse;        // <-- thêm
import com.english.api.content.dto.response.CommentResponse;
import com.english.api.content.service.ContentCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/content/comments")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
public class AdminCommentController {

    private final ContentCommentService service;

    @GetMapping("/by-post/{postId}")
    public ResponseEntity<PaginationResponse> listByPost(@PathVariable UUID postId, Pageable pageable) { 
        return ResponseEntity.ok(service.listByPost(postId, pageable, true));
    }

    @GetMapping("")
    public ResponseEntity<PaginationResponse> allComment(Pageable pageable) {
        return ResponseEntity.ok(service.showAllComment(pageable));
    }

    @PostMapping("/{id}/hide")
    public ResponseEntity<CommentResponse> hide(@PathVariable UUID id) {
        return ResponseEntity.ok(service.hide(id));
    }

    @PostMapping("/{id}/unhide")
    public ResponseEntity<CommentResponse> unhide(@PathVariable UUID id) {
        return ResponseEntity.ok(service.unhide(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
