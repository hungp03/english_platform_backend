package com.english.api.forum.service.impl;

import com.english.api.auth.util.SecurityUtil;
import com.english.api.common.dto.PaginationResponse;
import com.english.api.forum.dto.request.ForumReportCreateRequest;
import com.english.api.forum.dto.response.ForumReportResponse;
import com.english.api.forum.entity.ForumPost;
import com.english.api.forum.entity.ForumReport;
import com.english.api.forum.entity.ForumThread;
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
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ForumReportServiceImpl implements ForumReportService {

    private final ForumReportRepository reportRepository;
    private final ForumPostRepository postRepository;
    private final ForumThreadRepository threadRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    @Override
    @Transactional
    public ForumReportResponse create(ForumReportCreateRequest req) {
        UUID userId = SecurityUtil.getCurrentUserId();
        
        User user = userRepository.getReferenceById(userId);

        var entity = ForumReport.builder()
                .targetType(req.targetType())
                .targetId(req.targetId())
                .user(user) // Set entity User
                .reason(req.reason())
                .build();
        
        entity = reportRepository.save(entity);
        return toDto(entity);
    }

    @Override
    public PaginationResponse list(ReportTargetType type, boolean onlyOpen, Pageable pageable) {

        Page<ForumReport> page = onlyOpen
                ? reportRepository.findByTargetTypeAndResolvedAtIsNull(type, pageable)
                : reportRepository.findByTargetType(type, pageable);

        var reports = page.getContent();

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


        var postMap = postRepository.findAllById(postIds).stream()
                .collect(java.util.stream.Collectors.toMap(p -> p.getId(), p -> p));

        var threadMap = threadRepository.findAllById(threadIds).stream()
                .collect(java.util.stream.Collectors.toMap(t -> t.getId(), t -> t));

        var mapped = reports.stream().map(r -> {
            String preview = null;
            Boolean targetPublished = null;

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
        User admin = userRepository.getReferenceById(adminId);

        var r = reportRepository.findById(reportId).orElseThrow();
        r.setResolvedAt(Instant.now());
        r.setResolvedBy(admin); // Set entity Admin

        if (r.getUser() != null) {
            notificationService.sendNotification(
               r.getUser().getId(),
               "Report Resolved",
               "Your report regarding a " + r.getTargetType().toString().toLowerCase() + " has been reviewed and resolved."
           );
       }
        
        return toDto(reportRepository.save(r));
    }

    private ForumReportResponse toDto(ForumReport r) {
        String preview = null;
        Boolean targetPublished = null;
        
        // Fetch lẻ target (dùng cho create/update đơn lẻ)
        if (r.getTargetType() == ReportTargetType.POST) {
            var p = postRepository.findById(r.getTargetId()).orElse(null);
            if (p != null) {
                preview = p.getBodyMd();
                targetPublished = p.isPublished();
            }
        } else if (r.getTargetType() == ReportTargetType.THREAD) {
            var t = threadRepository.findById(r.getTargetId()).orElse(null);
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
