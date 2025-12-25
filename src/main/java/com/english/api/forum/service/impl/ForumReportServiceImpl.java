package com.english.api.forum.service.impl;

import com.english.api.auth.util.SecurityUtil;
import com.english.api.common.dto.PaginationResponse;
import com.english.api.forum.dto.request.ForumReportCreateRequest;
import com.english.api.forum.dto.response.ForumReportResponse;
import com.english.api.forum.repository.ForumPostRepository;
import com.english.api.forum.repository.ForumReportRepository;
import com.english.api.forum.repository.ForumThreadRepository;
import com.english.api.forum.service.ForumReportService;
import com.english.api.forum.mapper.ForumReportMapper;
import com.english.api.forum.model.ForumPost;
import com.english.api.forum.model.ForumReport;
import com.english.api.forum.model.ForumThread;
import com.english.api.forum.model.ReportTargetType;
import com.english.api.user.model.User;
import com.english.api.user.repository.UserRepository;
import com.english.api.course.model.CourseReview;
import com.english.api.course.repository.CourseReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.english.api.notification.service.NotificationService;
import java.time.Instant;

import java.util.List;
import java.util.Map;
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
    private final ForumReportMapper reportMapper;
    private final CourseReviewRepository courseReviewRepository;
    
    @Override
    @Transactional
    public ForumReportResponse create(ForumReportCreateRequest request) {
        UUID userId = SecurityUtil.getCurrentUserId();
        
        User user = userRepository.getReferenceById(userId);

        ForumReport report = ForumReport.builder()
                .targetType(request.targetType())
                .targetId(request.targetId())
                .user(user)
                .reason(request.reason())
                .build();
        
        report = reportRepository.save(report);
        return reportMapper.toResponse(report);
    }

    @Override
    public PaginationResponse list(ReportTargetType type, boolean onlyOpen, Pageable pageable) {

        Page<ForumReport> page = onlyOpen
                ? reportRepository.findByTargetTypeAndResolvedAtIsNull(type, pageable)
                : reportRepository.findByTargetType(type, pageable);

        List<ForumReport> reports = page.getContent();

        List<UUID> postIds = reports.stream()
                .filter(report -> report.getTargetType() == ReportTargetType.POST)
                .map(ForumReport::getTargetId)
                .distinct()
                .toList();

        List<UUID> threadIds = reports.stream()
                .filter(report -> report.getTargetType() == ReportTargetType.THREAD)
                .map(ForumReport::getTargetId)
                .distinct()
                .toList();
        
        List<UUID> reviewIds = reports.stream()
                .filter(report -> report.getTargetType() == ReportTargetType.COURSE_REVIEW)
                .map(ForumReport::getTargetId)
                .distinct()
                .toList();

        Map<UUID, ForumPost> postMap = postRepository.findAllById(postIds).stream()
                .collect(Collectors.toMap(ForumPost::getId, post -> post));

        Map<UUID, ForumThread> threadMap = threadRepository.findAllById(threadIds).stream()
                .collect(Collectors.toMap(ForumThread::getId, thread -> thread));
        
        Map<UUID, CourseReview> reviewMap = courseReviewRepository.findAllById(reviewIds).stream()
                .collect(Collectors.toMap(CourseReview::getId, review -> review));
        // String reviewSlug = new String();         

        List<ForumReportResponse> mapped = reports.stream().map(report -> {
            String preview = null;
            Boolean targetPublished = null;
            String reviewSlug = new String();
            if (report.getTargetType() == ReportTargetType.POST) {
                ForumPost post = postMap.get(report.getTargetId());
                if (post != null) {
                    preview = post.getBodyMd();
                    targetPublished = post.isPublished();
                }
            } else if (report.getTargetType() == ReportTargetType.THREAD) {
                ForumThread thread = threadMap.get(report.getTargetId());
                if (thread != null) {
                    preview = thread.getSlug();
                    targetPublished = !thread.isLocked();
                }
            } else if (report.getTargetType() == ReportTargetType.COURSE_REVIEW) { 
                // Xử lý hiển thị cho Course Review
                CourseReview review = reviewMap.get(report.getTargetId());
                if (review != null) {
                    preview = review.getComment(); // Hiển thị nội dung comment
                    reviewSlug = review.getCourse().getSlug();
                    targetPublished = review.getIsPublished();
                }
            }
            
            String reporterName = report.getUser() != null ? report.getUser().getFullName() : "Unknown";
            String reporterEmail = report.getUser() != null ? report.getUser().getEmail() : null;

            assert report.getUser() != null;
            return new ForumReportResponse(
                    report.getId(),
                    report.getTargetType(),
                    report.getTargetId(),
                    report.getUser().getId(),
                    reporterName,
                    reporterEmail,
                    report.getReason(),
                    preview,
                    targetPublished,
                    report.getCreatedAt(),
                    report.getResolvedAt(),
                    reviewSlug,
                    report.getResolvedBy() != null ? report.getResolvedBy().getFullName() : null
            );
        }).toList();

        return PaginationResponse.from(new PageImpl<>(mapped, pageable, page.getTotalElements()), pageable);
    }

    @Override
    @Transactional
    public ForumReportResponse resolve(UUID reportId) {
        UUID adminId = SecurityUtil.getCurrentUserId();
        User admin = userRepository.getReferenceById(adminId);

        ForumReport report = reportRepository.findById(reportId).orElseThrow();
        report.setResolvedAt(Instant.now());
        report.setResolvedBy(admin); // Set entity Admin

        if (report.getUser() != null) {
            notificationService.sendNotification(
               report.getUser().getId(),
               "Report Resolved",
               "Your report regarding a " + report.getTargetType().toString().toLowerCase() + " has been reviewed and resolved."
           );
       }
        
        return reportMapper.toResponse(reportRepository.save(report));
    }
}
