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
import com.english.api.notification.service.NotificationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageImpl;
@Service
@RequiredArgsConstructor
public class BlogCommentServiceImpl implements BlogCommentService {

    private final BlogCommentRepository commentRepository;
    private final BlogPostRepository postRepository;
    private final UserService userService;
    private final BlogMapper blogMapper;
    private final NotificationService notificationService;

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

        // Send notification to post author (if commenter is not the author)
        if (!post.getAuthor().getId().equals(currentUserId)) {
            notificationService.sendNotification(
                post.getAuthor().getId(),
                "Bình luận mới trong bài viết của bạn",
                author.getFullName() + " đã bình luận trong bài viết của bạn: \"" + post.getTitle() + "\""
            );
        }
        
        // If this is a reply, send notification to parent comment author
        if (parent != null && !parent.getAuthor().getId().equals(currentUserId)) {
            notificationService.sendNotification(
                parent.getAuthor().getId(),
                "New Reply to Your Comment",
                author.getFullName() + " replied to your comment on \"" + post.getTitle() + "\""
            );
        }
        
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

     @Override
     public PaginationResponse listByPost(UUID postId, Pageable pageable, boolean includeUnpublished) {
        BlogPost post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

//     // Lấy root comments (theo quyền: admin thấy cả ẩn, user thường chỉ thấy public)
//     Page<BlogComment> rootsPage = commentRepository.findRootsByPost(postId, includeUnpublished, pageable);

//     if (rootsPage.isEmpty()) {
//         return PaginationResponse.from(Page.empty(pageable), pageable);
//     }

//     List<UUID> rootIds = rootsPage.getContent().stream()
//             .map(BlogComment::getId)
//             .toList();

//     List<BlogComment> childComments = commentRepository.findChildrenByParentIds(rootIds, includeUnpublished);

//     Map<UUID, List<BlogComment>> childrenMap = childComments.stream()
//             .collect(Collectors.groupingBy(c -> c.getParent().getId()));

//     List<CommentResponse> flattenedList = new ArrayList<>();
//     for (BlogComment root : rootsPage.getContent()) {
//         flattenedList.add(blogMapper.toCommentResponse(root));

//         List<BlogComment> children = childrenMap.getOrDefault(root.getId(), List.of());
//         children.stream()
//                 .sorted(Comparator.comparing(BlogComment::getCreatedAt))
//                 .map(blogMapper::toCommentResponse)
//                 .forEach(flattenedList::add);
//     }

//     // QUAN TRỌNG: Luôn trả về tổng số comment CÔNG KHAI cho frontend
//     // → Dù admin đang xem cả comment ẩn, người dùng vẫn thấy "237 bình luận" (chỉ public)
//     long totalPublicComments = commentRepository.countPublicByPostId(postId);

//     Page<CommentResponse> resultPage = new PageImpl<>(
//             flattenedList,
//             pageable,
//             rootsPage.getTotalElements()   // ← Đây là số frontend hiển thị: "của 237 bình luận"
//     );

//     return PaginationResponse.from(resultPage, pageable);
// }


// @Override
// public PaginationResponse listByPost(UUID postId, Pageable pageable, boolean includeUnpublished) {
//     if (!postRepository.existsById(postId)) {
//         throw new ResourceNotFoundException("Post not found");
//     }

//     // Bước 1: Lấy đúng 10 comment CHA (root) mỗi trang
//     Page<BlogComment> rootsPage = includeUnpublished
//         ? commentRepository.findAllRootComments(postId, pageable)
//         : commentRepository.findPublicRootComments(postId, pageable);

//     // Nếu không có comment nào → trả về rỗng chuẩn
//     if (!rootsPage.hasContent()) {
//         return PaginationResponse.from(Page.empty(pageable), pageable);
//     }

//     // Bước 2: Lấy ID của 10 cha này
//     List<UUID> rootIds = rootsPage.getContent().stream()
//             .map(BlogComment::getId)
//             .toList();

//     // Bước 3: Lấy HẾT comment con của 10 cha đó (không phân trang con)
//     List<BlogComment> replies = commentRepository.findRepliesByParentIds(rootIds);

//     // Gom nhóm reply theo parentId
//     Map<UUID, List<BlogComment>> repliesByParent = replies.stream()
//             .collect(Collectors.groupingBy(c -> c.getParent().getId()));

//     // Bước 4: Tạo danh sách phẳng: cha → con → cha → con...
//     List<CommentResponse> result = new ArrayList<>();

//     for (BlogComment root : rootsPage.getContent()) {
//         // Thêm cha
//         result.add(blogMapper.toCommentResponse(root));

//         // Thêm hết con (nếu có), sort cũ → mới
//         // List<BlogComment> children = repliesByParent.getOrDefault(root.getId());
//         List<BlogComment> children = repliesByParent.getOrDefault(root.getId(), Collections.emptyList());
//         if (children != null && !children.isEmpty()) {
//             children.stream()
//                     .sorted(Comparator.comparing(BlogComment::getCreatedAt))
//                     .map(blogMapper::toCommentResponse)
//                     .forEach(result::add);
//         }
//     }

//     // Bước 5: Tạo Page với total đúng = số comment cha (root)
//     Page<CommentResponse> page = new PageImpl<>(
//             result,
//             pageable,
//             rootsPage.getTotalElements() // ← luôn đúng nhờ countQuery
//     );

//     return PaginationResponse.from(page, pageable);
// }

// @Override
// public PaginationResponse listByPost(UUID postId, Pageable pageable, boolean includeUnpublished) {
//     // 1. Check if post exists
//     if (!postRepository.existsById(postId)) {
//         throw new ResourceNotFoundException("Post not found");
//     }

//     // 2. Fetch Roots (Threads) Only
//     // This dictates the structure. Page 1 = First 10 conversations.
//     Page<BlogComment> rootsPage = includeUnpublished
//         ? commentRepository.findAllRootComments(postId, pageable)
//         : commentRepository.findPublicRootComments(postId, pageable);

//     if (rootsPage.isEmpty()) {
//         return PaginationResponse.from(Page.empty(pageable), pageable);
//     }

//     // 3. Fetch All Replies for these specific roots
//     List<UUID> rootIds = rootsPage.getContent().stream()
//             .map(BlogComment::getId)
//             .toList();

//     // Use the optimized repository method to fetch replies
//     List<BlogComment> replies = commentRepository.findRepliesByParentIds(rootIds);

//     // Group replies by their parent ID
//     Map<UUID, List<BlogComment>> repliesByParent = replies.stream()
//             .collect(Collectors.groupingBy(c -> c.getParent().getId()));

//     // 4. Flatten the list: Root A -> Replies A -> Root B -> Replies B
//     List<CommentResponse> flatResult = new ArrayList<>();
    
//     for (BlogComment root : rootsPage.getContent()) {
//         // Add Root
//         flatResult.add(blogMapper.toCommentResponse(root));

//         // Add Children (Sorted by oldest first)
//         List<BlogComment> children = repliesByParent.getOrDefault(root.getId(), Collections.emptyList());
//         children.stream()
//                 .sorted(Comparator.comparing(BlogComment::getCreatedAt))
//                 .map(blogMapper::toCommentResponse)
//                 .forEach(flatResult::add);
//     }

//     // 5. Construct Page using ROOT COUNT
//     // CRITICAL: The totalElements must be rootsPage.getTotalElements() (e.g., 15 threads), 
//     // NOT flatResult.size() (e.g., 59 comments).
//     Page<CommentResponse> page = new PageImpl<>(
//             flatResult,
//             pageable,
//             rootsPage.getTotalElements() 
//     );

//     return PaginationResponse.from(page, pageable);
// }

// @Override
// public PaginationResponse listByPost(UUID postId, Pageable pageable, boolean includeUnpublished) {
//     // 1. Kiểm tra post tồn tại
//     if (!postRepository.existsById(postId)) {
//         throw new ResourceNotFoundException("Post not found");
//     }

//     // 2. Lấy danh sách Root Comment (Comment cha) theo phân trang
//     // countQuery trong Repository sẽ đảm bảo totalElements chỉ đếm số Root
//     Page<BlogComment> rootsPage = includeUnpublished
//         ? commentRepository.findAllRootComments(postId, pageable)
//         : commentRepository.findPublicRootComments(postId, pageable);

//     // Nếu trang này không có root nào (ví dụ user request page 100), trả về rỗng ngay
//     if (rootsPage.getContent().isEmpty()) {
//         return PaginationResponse.from(Page.empty(pageable), pageable);
//     }

//     // 3. Lấy ID của các Root để tìm con
//     List<UUID> rootIds = rootsPage.getContent().stream()
//             .map(BlogComment::getId)
//             .toList();

//     // 4. Lấy TẤT CẢ reply của các Root này (Không phân trang reply)
//     // Dùng phương pháp IN queries để tránh lỗi N+1
//     List<BlogComment> replies = commentRepository.findRepliesByParentIds(rootIds);

//     // Gom nhóm reply theo ParentID
//     Map<UUID, List<BlogComment>> repliesByParent = replies.stream()
//             .collect(Collectors.groupingBy(c -> c.getParent().getId()));

//     // 5. Sắp xếp danh sách phẳng: Root -> Replies của nó -> Root tiếp theo
//     List<CommentResponse> flatResult = new ArrayList<>();

//     for (BlogComment root : rootsPage.getContent()) {
//         // Add Root
//         flatResult.add(blogMapper.toCommentResponse(root));

//         // Add Children (Sắp xếp cũ nhất -> mới nhất)
//         List<BlogComment> children = repliesByParent.getOrDefault(root.getId(), Collections.emptyList());
//         if (!children.isEmpty()) {
//             children.stream()
//                     .sorted(Comparator.comparing(BlogComment::getCreatedAt))
//                     .map(blogMapper::toCommentResponse)
//                     .forEach(flatResult::add);
//         }
//     }

// @Override
// public PaginationResponse listByPost(UUID postId, Pageable pageable, boolean includeUnpublished) {
//     if (!postRepository.existsById(postId)) {
//         throw new ResourceNotFoundException("Post not found");
//     }

//     // 1. Query lấy Root (Cha) - Đã bao gồm countQuery đếm số Root trong Repository
//     Page<BlogComment> rootsPage = includeUnpublished
//         ? commentRepository.findAllRootComments(postId, pageable)
//         : commentRepository.findPublicRootComments(postId, pageable);

//     if (rootsPage.isEmpty()) {
//         return PaginationResponse.from(Page.empty(pageable), pageable);
//     }

//     // 2. Lấy replies cho các cha này
//     List<UUID> rootIds = rootsPage.getContent().stream().map(BlogComment::getId).toList();
//     List<BlogComment> replies = commentRepository.findRepliesByParentIds(rootIds);
//     Map<UUID, List<BlogComment>> repliesByParent = replies.stream()
//             .collect(Collectors.groupingBy(c -> c.getParent().getId()));

//     // 3. Gộp Cha và Con vào list phẳng
//     List<CommentResponse> flatResult = new ArrayList<>();
//     for (BlogComment root : rootsPage.getContent()) {
//         flatResult.add(blogMapper.toCommentResponse(root));
        
//         List<BlogComment> children = repliesByParent.getOrDefault(root.getId(), Collections.emptyList());
//         children.stream()
//                 .sorted(Comparator.comparing(BlogComment::getCreatedAt))
//                 .map(blogMapper::toCommentResponse)
//                 .forEach(flatResult::add);
//     }

//     // 4. QUAN TRỌNG NHẤT: Sử dụng rootsPage.getTotalElements()
//     // Total này là số lượng Thread (ví dụ: 15), KHÔNG PHẢI tổng comment (59)
//     Page<CommentResponse> page = new PageImpl<>(
//             flatResult, 
//             pageable, 
//             rootsPage.getTotalElements() // <-- SỬA LẠI CHỖ NÀY
//     );

//     return PaginationResponse.from(page, pageable);
// }
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

//     // --- KHẮC PHỤC LỖI GHOST PAGE TẠI ĐÂY ---
//     // SAI: rootsPage.getTotalElements() + replies.size() hoặc countAllByPostId()
//     // ĐÚNG: rootsPage.getTotalElements() (Số lượng cuộc hội thoại gốc)
    
//     Page<CommentResponse> page = new PageImpl<>(
//             flatResult,                 // Dữ liệu hiển thị (bao gồm cả con)
//             pageable,                   // Thông tin trang hiện tại (size=10)
//             rootsPage.getTotalElements() // QUAN TRỌNG: Chỉ đếm số Root (ví dụ: 15)
//     );

//     return PaginationResponse.from(page, pageable);
// }

//         @Override
//     public PaginationResponse listByPost(UUID postId, Pageable pageable, boolean includeUnpublished) {
//         // Kiểm tra post tồn tại [cite: 100]
//         if (!postRepository.existsById(postId)) {
//             throw new ResourceNotFoundException("Post not found");
//         }

//         // BƯỚC 1: Tìm các comment CHA (Root) theo trang (ví dụ: lấy 10 cha)
//         Page<BlogComment> rootsPage = commentRepository.findRootsByPost(postId, includeUnpublished, pageable);
//         long totalActualComments = commentRepository.countAllByPostId(postId, includeUnpublished);

//         if (rootsPage.isEmpty()) {
//             return PaginationResponse.from(rootsPage.map(blogMapper::toCommentResponse), pageable);
//         }

//         // Lấy danh sách ID của các cha
//         List<UUID> rootIds = rootsPage.getContent().stream()
//                 .map(BlogComment::getId)
//                 .toList();

//         // BƯỚC 2: Tìm TẤT CẢ comment CON của các cha này (không phân trang con)
//         List<BlogComment> childComments = commentRepository.findChildrenByParentIds(rootIds, includeUnpublished);

//         // BƯỚC 3: Sắp xếp và tổ chức lại dữ liệu để trả về
//         // Mục tiêu: Trả về List phẳng nhưng theo thứ tự: Cha A -> Con A1 -> Con A2 -> Cha B -> ...
        
//         // Map con theo Parent ID để dễ lấy
//         Map<UUID, List<BlogComment>> childrenMap = childComments.stream()
//                 .collect(Collectors.groupingBy(c -> c.getParent().getId()));

//         List<CommentResponse> sortedResponses = new ArrayList<>();

//         // Duyệt qua từng comment cha trong page hiện tại
//         for (BlogComment root : rootsPage.getContent()) {
//             // 1. Add Cha
//             sortedResponses.add(blogMapper.toCommentResponse(root));

//             // 2. Add tất cả Con của Cha đó (nếu có)
//             List<BlogComment> childrenOfThisRoot = childrenMap.get(root.getId());
//             if (childrenOfThisRoot != null) {
//                 // Sort con theo thời gian (cũ nhất lên đầu)
//                 childrenOfThisRoot.sort(Comparator.comparing(BlogComment::getCreatedAt));
                
//                 List<CommentResponse> childResponses = childrenOfThisRoot.stream()
//                         .map(blogMapper::toCommentResponse)
//                         .toList();
//                 sortedResponses.addAll(childResponses);
//             }
//         }

//         // BƯỚC 4: Tạo Page mới từ list đã gộp
//         // Lưu ý: "totalElements" ở đây vẫn là tổng số Root Comments (số cuộc hội thoại), 
//         // chứ không phải tổng số dòng comment. Điều này giúp Frontend tính số trang dựa trên số cuộc hội thoại.
//         Page<CommentResponse> resultPage = new PageImpl<>(
//                 sortedResponses,
//                 pageable,
//                 rootsPage.getTotalElements()
//         );

//         return PaginationResponse.from(resultPage, pageable);
// }

// @Override
// public PaginationResponse listByPost(UUID postId, Pageable pageable, boolean includeUnpublished) {
//     // Kiểm tra post tồn tại
//     if (!postRepository.existsById(postId)) {
//         throw new ResourceNotFoundException("Post not found");
//     }

//     // BƯỚC 1: Tìm các comment CHA (Root) theo trang
//     Page<BlogComment> rootsPage = commentRepository.findRootsByPost(postId, includeUnpublished, pageable);

//     if (rootsPage.isEmpty()) {
//         return PaginationResponse.from(rootsPage.map(blogMapper::toCommentResponse), pageable);
//     }

//     // Lấy danh sách ID của các cha
//     List<UUID> rootIds = rootsPage.getContent().stream()
//             .map(BlogComment::getId)
//             .toList();

//     // BƯỚC 2: Tìm TẤT CẢ comment CON của các cha này
//     List<BlogComment> childComments = commentRepository.findChildrenByParentIds(rootIds, includeUnpublished);

//     // BƯỚC 3: Sắp xếp và tổ chức lại dữ liệu
//     Map<UUID, List<BlogComment>> childrenMap = childComments.stream()
//             .collect(Collectors.groupingBy(c -> c.getParent().getId()));

//     List<CommentResponse> sortedResponses = new ArrayList<>();

//     // Duyệt qua từng comment cha trong page hiện tại
//     for (BlogComment root : rootsPage.getContent()) {
//         // 1. Add Cha
//         sortedResponses.add(blogMapper.toCommentResponse(root));

//         // 2. Add tất cả Con của Cha đó (nếu có)
//         List<BlogComment> childrenOfThisRoot = childrenMap.get(root.getId());
//         if (childrenOfThisRoot != null) {
//             // Sort con theo thời gian (cũ nhất lên đầu)
//             childrenOfThisRoot.sort(Comparator.comparing(BlogComment::getCreatedAt));
            
//             List<CommentResponse> childResponses = childrenOfThisRoot.stream()
//                     .map(blogMapper::toCommentResponse)
//                     .toList();
//             sortedResponses.addAll(childResponses);
//         }
//     }

//     // BƯỚC 4: Đếm tổng số comment PUBLIC (chỉ đếm comment published = true)
//     long totalPublicComments = commentRepository.countAllByPostId(postId, false); // false = chỉ đếm public
    
//     // BƯỚC 5: Tính số trang dựa trên tổng comment public
//     int actualTotalPages = (int) Math.ceil((double) totalPublicComments / pageable.getPageSize());
    
//     // Kiểm tra nếu page request vượt quá số trang thực tế
//     if (pageable.getPageNumber() >= actualTotalPages && actualTotalPages > 0) {
//         // Trả về empty page với metadata đúng
//         Page<CommentResponse> emptyPage = new PageImpl<>(
//             new ArrayList<>(),
//             pageable,
//             totalPublicComments
//         );
//         return PaginationResponse.from(emptyPage, pageable);
//     }
    
//     // Tạo Page với tổng số comment public
//     Page<CommentResponse> resultPage = new PageImpl<>(
//             sortedResponses,
//             pageable,
//             totalPublicComments  // Tổng số comment PUBLIC
//     );

//     return PaginationResponse.from(resultPage, pageable);
// }


// @Override
// public PaginationResponse listByPost(UUID postId, Pageable pageable, boolean includeUnpublished) {
//     BlogPost post = postRepository.findById(postId)
//             .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

//     // First query: get paginated comment IDs
//     Page<BlogComment> commentsPage = includeUnpublished
//             ? commentRepository.findByPost(post, pageable)
//             : commentRepository.findByPostAndPublishedIsTrue(post, pageable);

//     // If no results, return empty page
//     if (commentsPage.isEmpty()) {
//         return PaginationResponse.from(commentsPage.map(blogMapper::toCommentResponse), pageable);
//     }

//     // Second query: fetch full entities with associations
//     List<UUID> commentIds = commentsPage.getContent().stream()
//             .map(BlogComment::getId)
//             .toList();

//     List<BlogComment> commentsWithAssociations = commentRepository.findByIdInWithAssociations(commentIds);

//     // Preserve the original order from pagination
//     java.util.Map<UUID, BlogComment> commentMap = commentsWithAssociations.stream()
//             .collect(java.util.stream.Collectors.toMap(BlogComment::getId, c -> c));

//     List<CommentResponse> responses = commentIds.stream()
//             .map(commentMap::get)
//             .map(blogMapper::toCommentResponse)
//             .toList();

//     Page<CommentResponse> page = new org.springframework.data.domain.PageImpl<>(
//             responses, pageable, commentsPage.getTotalElements()
//     );

//     return PaginationResponse.from(page, pageable);
// }

//         @Override
//     public PaginationResponse listByPost(UUID postId, Pageable pageable, boolean includeUnpublished) {
//         // Kiểm tra bài viết tồn tại
//         if (!postRepository.existsById(postId)) {
//             throw new ResourceNotFoundException("Post not found");
//         }

//         // B1: Lấy trang các comment CHA (root) - phân trang theo thread
//         Page<BlogComment> rootsPage = commentRepository.findRootsByPost(postId, includeUnpublished, pageable);

//         // Nếu không có comment nào
//         if (rootsPage.isEmpty()) {
//             return PaginationResponse.from(Page.empty(pageable), pageable);
//         }

//         // Lấy ID các comment cha trong trang hiện tại
//         List<UUID> rootIds = rootsPage.getContent().stream()
//                 .map(BlogComment::getId)
//                 .toList();

//         // B2: Lấy TẤT CẢ comment con của các cha này (không phân trang con)
//         List<BlogComment> childComments = commentRepository.findChildrenByParentIds(rootIds, includeUnpublished);

//         // B3: Gom nhóm con theo parentId để dễ truy cập
//         Map<UUID, List<BlogComment>> childrenMap = childComments.stream()
//                 .collect(Collectors.groupingBy(c -> c.getParent().getId()));

//         // B4: Tạo danh sách phẳng: Cha A → con của A → Cha B → con của B...
//         List<CommentResponse> flattenedList = new ArrayList<>();

//         for (BlogComment root : rootsPage.getContent()) {
//             // Thêm comment cha
//             flattenedList.add(blogMapper.toCommentResponse(root));

//             // Thêm tất cả con (nếu có), sắp xếp cũ → mới
//             List<BlogComment> children = childrenMap.getOrDefault(root.getId(), List.of());
//             children.stream()
//                     .sorted(Comparator.comparing(BlogComment::getCreatedAt))
//                     .map(blogMapper::toCommentResponse)
//                     .forEach(flattenedList::add);
//         }

//         // B5: Đếm tổng số bình luận thực tế trong bài viết (root + tất cả con)
//         long totalActualComments = commentRepository.countAllByPostId(postId, includeUnpublished);

//         // B6: Tạo Page kết quả với totalElements đúng (tổng bình luận thật)
//         Page<CommentResponse> resultPage = new PageImpl<>(
//                 flattenedList,
//                 pageable,
//                 totalActualComments
//         );

//         return PaginationResponse.from(resultPage, pageable);
//     }

        // @Override
        // public PaginationResponse listByPost(UUID postId, Pageable pageable, boolean includeUnpublished) {
        // BlogPost post = postRepository.findById(postId)
        //         .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        // // CHỈ LẤY COMMENT GỐC (parent IS NULL)
        // Page<BlogComment> rootsPage = includeUnpublished
        //         ? commentRepository.findRootCommentsByPost(post, pageable)   // tất cả root
        //         : commentRepository.findRootCommentsByPostAndPublished(post, pageable); // chỉ root published

        // if (rootsPage.isEmpty()) {
        //         return PaginationResponse.from(rootsPage.map(blogMapper::toCommentResponse), pageable);
        // }

        // // Lấy danh sách ID của các root comment trong page hiện tại
        // List<UUID> rootIds = rootsPage.getContent().stream()
        //         .map(BlogComment::getId)
        //         .toList();

        // // Load đầy đủ entity + associations chỉ cho các root này (tránh N+1)
        // List<BlogComment> rootsWithAssociations = commentRepository.findByIdInWithAssociations(rootIds);

        // // Giữ nguyên thứ tự phân trang
        // Map<UUID, BlogComment> commentMap = rootsWithAssociations.stream()
        //         .collect(Collectors.toMap(BlogComment::getId, c -> c));

        // List<CommentResponse> responses = rootIds.stream()
        //         .map(commentMap::get)
        //         .map(blogMapper::toCommentResponse)
        //         .toList();

        // Page<CommentResponse> page = new PageImpl<>(
        //         responses,
        //         pageable,
        //         rootsPage.getTotalElements()  // tổng số root comment, không phải tổng comment
        // );

        // return PaginationResponse.from(page, pageable);
        // }


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
