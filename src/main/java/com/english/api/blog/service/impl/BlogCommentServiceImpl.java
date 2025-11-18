package com.english.api.blog.service.impl;

import com.english.api.auth.util.SecurityUtil;
import com.english.api.common.dto.PaginationResponse;
import com.english.api.common.exception.ResourceNotFoundException;
import com.english.api.blog.dto.request.CommentCreateRequest;
import com.english.api.blog.dto.request.CommentUpdateRequest;
import com.english.api.blog.dto.response.CommentResponse;
import com.english.api.blog.mapper.BlogMapper;
import com.english.api.blog.model.BlogComment;
import com.english.api.blog.model.BlogPost;
import com.english.api.blog.repository.BlogCommentRepository;
import com.english.api.blog.repository.BlogPostRepository;
import com.english.api.blog.service.BlogCommentService;
import com.english.api.user.model.User;
import com.english.api.user.service.UserService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BlogCommentServiceImpl implements BlogCommentService {

    private final BlogCommentRepository commentRepository;
    private final BlogPostRepository postRepository;
    private final UserService userService;
    private final BlogMapper blogMapper;

    @Override
    @Transactional
    public CommentResponse create(UUID postId, CommentCreateRequest req) {
        BlogPost post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        BlogComment parent = null;
        if (req.parentId() != null) {
            parent = commentRepository.findById(req.parentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent comment not found"));
        }

        UUID currentUserId = SecurityUtil.getCurrentUserId();
        User author = userService.findById(currentUserId);

        BlogComment c = BlogComment.builder()
                .post(post)
                .parent(parent)
                .author(author)
                .bodyMd(req.bodyMd())
                .published(true)
                .build();
        c = commentRepository.save(c);
        return blogMapper.toCommentResponse(c);
    }

    @Override
    @Transactional
    public CommentResponse update(UUID commentId, CommentUpdateRequest req) {
        BlogComment c = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        c.setBodyMd(req.bodyMd());
        c = commentRepository.save(c);
        return blogMapper.toCommentResponse(c);
    }

    @Override
    @Transactional
    public void delete(UUID commentId) {
        BlogComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        // nếu là comment cấp 1 thì xoá luôn tất cả con
        if (comment.getParent() == null) {
                commentRepository.deleteByParent(comment);
        }

        // xoá chính nó
        commentRepository.delete(comment);
    }

    @Override
    @Transactional
    public CommentResponse hide(UUID commentId) {
        BlogComment c = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        c.setPublished(false);
        c = commentRepository.save(c);
        return blogMapper.toCommentResponse(c);
    }

    @Override
    @Transactional
    public CommentResponse unhide(UUID commentId) {
        BlogComment c = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        c.setPublished(true);
        c = commentRepository.save(c);
        return blogMapper.toCommentResponse(c);
    }

//     @Override
//     public PaginationResponse listByPost(UUID postId, Pageable pageable, boolean includeUnpublished) {
//         BlogPost post = postRepository.findById(postId)
//                 .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

//         // First query: get paginated comment IDs
//         Page<BlogComment> commentsPage = includeUnpublished
//                 ? commentRepository.findByPost(post, pageable)
//                 : commentRepository.findByPostAndPublishedIsTrue(post, pageable);

//         // If no results, return empty page
//         if (commentsPage.isEmpty()) {
//             return PaginationResponse.from(commentsPage.map(blogMapper::toCommentResponse), pageable);
//         }

//         // Second query: fetch full entities with associations
//         List<UUID> commentIds = commentsPage.getContent().stream()
//                 .map(BlogComment::getId)
//                 .toList();

//         List<BlogComment> commentsWithAssociations = commentRepository.findByIdInWithAssociations(commentIds);

//         // Preserve the original order from pagination
//         java.util.Map<UUID, BlogComment> commentMap = commentsWithAssociations.stream()
//                 .collect(java.util.stream.Collectors.toMap(BlogComment::getId, c -> c));

//         List<CommentResponse> responses = commentIds.stream()
//                 .map(commentMap::get)
//                 .map(blogMapper::toCommentResponse)
//                 .toList();

//         Page<CommentResponse> page = new org.springframework.data.domain.PageImpl<>(
//                 responses, pageable, commentsPage.getTotalElements()
//         );

//         return PaginationResponse.from(page, pageable);
//     }
        @Override
        public PaginationResponse listByPost(UUID postId, Pageable pageable, boolean includeUnpublished) {
        BlogPost post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        // 1. Lấy page comment như hiện tại
        Page<BlogComment> commentsPage = includeUnpublished
                ? commentRepository.findByPost(post, pageable)
                : commentRepository.findByPostAndPublishedIsTrue(post, pageable);

        // Nếu không có kết quả thì trả về luôn
        if (commentsPage.isEmpty()) {
                return PaginationResponse.from(
                        commentsPage.map(blogMapper::toCommentResponse),
                        pageable
                );
        }

        // 2. Danh sách ID comment trong page hiện tại
        List<UUID> commentIds = commentsPage.getContent().stream()
                .map(BlogComment::getId)
                .toList();

        // 3. Load đầy đủ entity + associations cho các comment này
        List<BlogComment> commentsWithAssociations =
                commentRepository.findByIdInWithAssociations(commentIds);

        // Tập ID comment trong page (để biết parent nào đã nằm trong page)
        java.util.Set<UUID> commentIdSet = new java.util.HashSet<>(commentIds);

        // 4. Tìm các parentId còn thiếu (parent không nằm trong page hiện tại)
        java.util.Set<UUID> missingParentIds = commentsWithAssociations.stream()
                .map(BlogComment::getParent)
                .filter(java.util.Objects::nonNull)
                .map(BlogComment::getId)
                .filter(parentId -> !commentIdSet.contains(parentId))
                .collect(java.util.stream.Collectors.toSet());

        // Map id -> comment để giữ đúng thứ tự ban đầu cho 20 comment chính
        java.util.Map<UUID, BlogComment> commentMap = commentsWithAssociations.stream()
                .collect(java.util.stream.Collectors.toMap(BlogComment::getId, c -> c));

        // 5. Map 20 comment chính sang DTO, giữ nguyên thứ tự phân trang
        List<CommentResponse> mainResponses = commentIds.stream()
                .map(commentMap::get)
                .map(blogMapper::toCommentResponse)
                .toList();

        // 6. Nếu có parent nằm ngoài page, load thêm và map sang DTO
        List<CommentResponse> parentResponses = java.util.Collections.emptyList();
        if (!missingParentIds.isEmpty()) {
                List<BlogComment> missingParents =
                        commentRepository.findByIdInWithAssociations(
                                new java.util.ArrayList<>(missingParentIds)
                        );

                // (tuỳ chọn) sort parent theo createdAt cho ổn định
                missingParents.sort(java.util.Comparator.comparing(BlogComment::getCreatedAt));

                parentResponses = missingParents.stream()
                        .map(blogMapper::toCommentResponse)
                        .toList();
        }

        // 7. Gộp 20 comment chính + các parent bổ sung vào cùng result
        List<CommentResponse> allResponses =
                new java.util.ArrayList<>(mainResponses.size() + parentResponses.size());
        allResponses.addAll(mainResponses);
        allResponses.addAll(parentResponses);

        Page<CommentResponse> page = new org.springframework.data.domain.PageImpl<>(
                allResponses,
                pageable,
                commentsPage.getTotalElements()   // tổng bản ghi thực tế, KHÔNG cộng parent thêm
        );

        // 8. Trả về PaginationResponse như cũ
        return PaginationResponse.from(page, pageable);
        }


    @Override
    public PaginationResponse showAllComment(Pageable pageable) {
        // First query: get paginated comment IDs
        Page<BlogComment> commentsPage = commentRepository.findAll(pageable);

        // If no results, return empty page
        if (commentsPage.isEmpty()) {
            return PaginationResponse.from(commentsPage.map(blogMapper::toCommentResponse), pageable);
        }

        // Second query: fetch full entities with associations
        List<UUID> commentIds = commentsPage.getContent().stream()
                .map(BlogComment::getId)
                .toList();

        List<BlogComment> commentsWithAssociations = commentRepository.findByIdInWithAssociations(commentIds);

        // Preserve the original order from pagination
        java.util.Map<UUID, BlogComment> commentMap = commentsWithAssociations.stream()
                .collect(java.util.stream.Collectors.toMap(BlogComment::getId, c -> c));

        List<CommentResponse> responses = commentIds.stream()
                .map(commentMap::get)
                .map(blogMapper::toCommentResponse)
                .toList();

        Page<CommentResponse> page = new org.springframework.data.domain.PageImpl<>(
                responses, pageable, commentsPage.getTotalElements()
        );

        return PaginationResponse.from(page, pageable);
    }
}
