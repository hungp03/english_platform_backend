package com.english.api.forum.service.impl;

import com.english.api.auth.util.SecurityUtil;
import com.english.api.common.dto.PaginationResponse;
import com.english.api.forum.dto.request.ForumReportCreateRequest;
import com.english.api.forum.dto.response.ForumReportResponse;
import com.english.api.forum.entity.ForumReport;
import com.english.api.forum.entity.ReportTargetType;
import com.english.api.forum.repo.ForumPostRepository;
import com.english.api.forum.repo.ForumReportRepository;
import com.english.api.forum.repo.ForumThreadRepository;
import com.english.api.forum.service.ForumReportService;
import com.english.api.user.model.User;
import com.english.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.english.api.notification.service.NotificationService;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ForumReportServiceImpl implements ForumReportService {

    private final ForumReportRepository repo;
    private final ForumPostRepository postRepo;
    private final ForumThreadRepository threadRepo;
    private final UserRepository userRepo;
    private final NotificationService notificationService;
    @Override
    @Transactional
    public ForumReportResponse create(ForumReportCreateRequest req) {
        UUID userId = SecurityUtil.getCurrentUserId();
        // Sử dụng getReferenceById để lấy proxy (không tốn query DB nếu user tồn tại)
        User user = userRepo.getReferenceById(userId);

        var entity = ForumReport.builder()
                .targetType(req.targetType())
                .targetId(req.targetId())
                .user(user) // Set entity User
                .reason(req.reason())
                .build();
        
        entity = repo.save(entity);
        return toDto(entity);
    }

    @Override
    public PaginationResponse list(ReportTargetType type, boolean onlyOpen, Pageable pageable) {
        // Query DB (đã có @EntityGraph fetch user)
        Page<ForumReport> page = onlyOpen
                ? repo.findByTargetTypeAndResolvedAtIsNull(type, pageable)
                : repo.findByTargetType(type, pageable);

        var reports = page.getContent();

        // --- Logic tối ưu Target (Post/Thread) vẫn giữ nguyên vì targetId là Generic UUID ---
        var postIds = reports.stream()
                .filter(r -> r.getTargetType() == ReportTargetType.POST)
                .map(ForumReport::getTargetId)
                .distinct()
                .toList();

        var threadIds = reports.stream()
                .filter(r -> r.getTargetType() == ReportTargetType.THREAD)
                .map(ForumReport::getTargetId)
                .distinct()
                .toList();

        // Lưu ý: Đã XÓA đoạn code thủ công query UserMap ở đây vì User đã có trong Entity

        var postMap = postRepo.findAllById(postIds).stream()
                .collect(java.util.stream.Collectors.toMap(p -> p.getId(), p -> p));

        var threadMap = threadRepo.findAllById(threadIds).stream()
                .collect(java.util.stream.Collectors.toMap(t -> t.getId(), t -> t));

        // Map sang DTO
        var mapped = reports.stream().map(r -> {
            String preview = null;
            Boolean targetPublished = null;

            // Map Target info
            if (r.getTargetType() == ReportTargetType.POST) {
                var p = postMap.get(r.getTargetId());
                if (p != null) {
                    preview = p.getBodyMd();
                    targetPublished = p.isPublished();
                }
            } else if (r.getTargetType() == ReportTargetType.THREAD) {
                var t = threadMap.get(r.getTargetId());
                if (t != null) {
                    preview = t.getTitle();
                    targetPublished = !t.isLocked();
                }
            }

            // Lấy thông tin User trực tiếp từ Entity (không cần map)
            String reporterName = r.getUser() != null ? r.getUser().getFullName() : "Unknown";
            String reporterEmail = r.getUser() != null ? r.getUser().getEmail() : null;

            return new ForumReportResponse(
                    r.getId(),
                    r.getTargetType(),
                    r.getTargetId(),
                    r.getUser().getId(), // Lấy ID từ object User
                    reporterName,
                    reporterEmail,
                    r.getReason(),
                    preview,
                    targetPublished,
                    r.getCreatedAt(),
                    r.getResolvedAt(),
                    r.getResolvedBy() != null ? r.getResolvedBy().getId() : null // Lấy ID admin
            );
        }).toList();

        return PaginationResponse.from(new PageImpl<>(mapped, pageable, page.getTotalElements()), pageable);
    }

    @Override
    @Transactional
    public ForumReportResponse resolve(UUID reportId) {
        UUID adminId = SecurityUtil.getCurrentUserId();
        User admin = userRepo.getReferenceById(adminId);

        var r = repo.findById(reportId).orElseThrow();
        r.setResolvedAt(Instant.now());
        r.setResolvedBy(admin); // Set entity Admin

        if (r.getUser() != null) {
            notificationService.sendNotification(
               r.getUser().getId(),
               "Report Resolved",
               "Your report regarding a " + r.getTargetType().toString().toLowerCase() + " has been reviewed and resolved."
           );
       }
        
        return toDto(repo.save(r));
    }

    private ForumReportResponse toDto(ForumReport r) {
        String preview = null;
        Boolean targetPublished = null;
        
        // Fetch lẻ target (dùng cho create/update đơn lẻ)
        if (r.getTargetType() == ReportTargetType.POST) {
            var p = postRepo.findById(r.getTargetId()).orElse(null);
            if (p != null) {
                preview = p.getBodyMd();
                targetPublished = p.isPublished();
            }
        } else if (r.getTargetType() == ReportTargetType.THREAD) {
            var t = threadRepo.findById(r.getTargetId()).orElse(null);
            if (t != null) {
                preview = t.getTitle();
                targetPublished = !t.isLocked();
            }
        }

        return new ForumReportResponse(
                r.getId(),
                r.getTargetType(),
                r.getTargetId(),
                r.getUser() != null ? r.getUser().getId() : null,
                r.getUser() != null ? r.getUser().getFullName() : null,
                r.getUser() != null ? r.getUser().getEmail() : null,
                r.getReason(),
                preview,
                targetPublished,
                r.getCreatedAt(),
                r.getResolvedAt(),
                r.getResolvedBy() != null ? r.getResolvedBy().getId() : null
        );
    }
}