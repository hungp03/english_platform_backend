package com.english.api.forum.service.impl;

import com.english.api.auth.util.SecurityUtil;
import com.english.api.common.dto.PaginationResponse;
import com.english.api.forum.dto.request.ForumReportCreateRequest;
import com.english.api.forum.dto.response.ForumReportResponse;
import com.english.api.forum.entity.ForumPost;
import com.english.api.forum.entity.ForumReport;
import com.english.api.forum.entity.ForumThread;
import com.english.api.forum.entity.ReportTargetType;
import com.english.api.forum.mapper.ForumReportMapper;
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

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ForumReportServiceImpl implements ForumReportService {

    private final ForumReportRepository reportRepo;
    private final ForumPostRepository postRepo;
    private final ForumThreadRepository threadRepo;
    private final UserRepository userRepo;
    private final ForumReportMapper forumReportMapper;

    @Override
    @Transactional
    public ForumReportResponse create(ForumReportCreateRequest request) {
        UUID userId = SecurityUtil.getCurrentUserId();
        var report = ForumReport.builder()
                .targetType(request.targetType())
                .targetId(request.targetId())
                .userId(userId)
                .reason(request.reason())
                .build();
        report = reportRepo.save(report);
        return forumReportMapper.toResponseWithFetch(report);
    }

    @Override
    public PaginationResponse list(ReportTargetType type, boolean onlyOpen, Pageable pageable) {
        Page<ForumReport> page = onlyOpen
                ? reportRepo.findByTargetTypeAndResolvedAtIsNull(type, pageable)
                : reportRepo.findByTargetType(type, pageable);

        var reports = page.getContent();

        var postIds = reports.stream()
                .filter(report -> report.getTargetType() == ReportTargetType.POST)
                .map(ForumReport::getTargetId)
                .distinct()
                .toList();

        var threadIds = reports.stream()
                .filter(report -> report.getTargetType() == ReportTargetType.THREAD)
                .map(ForumReport::getTargetId)
                .distinct()
                .toList();

        var userIds = reports.stream()
                .map(ForumReport::getUserId)
                .filter(id -> id != null)
                .distinct()
                .toList();

        Map<UUID, ForumPost> postMap = postIds.isEmpty()
                ? Collections.emptyMap()
                : postRepo.findAllById(postIds).stream()
                        .collect(Collectors.toMap(ForumPost::getId, post -> post));

        Map<UUID, ForumThread> threadMap = threadIds.isEmpty()
                ? Collections.emptyMap()
                : threadRepo.findAllById(threadIds).stream()
                        .collect(Collectors.toMap(ForumThread::getId, thread -> thread));

        Map<UUID, User> userMap = userIds.isEmpty()
                ? Collections.emptyMap()
                : userRepo.findAllById(userIds).stream()
                        .collect(Collectors.toMap(User::getId, user -> user));

        var reportResponses = reports.stream()
                .map(report -> forumReportMapper.toResponse(report, userMap, postMap, threadMap))
                .toList();

        return PaginationResponse.from(new PageImpl<>(reportResponses, pageable, page.getTotalElements()), pageable);
    }

    @Override
    @Transactional
    public ForumReportResponse resolve(UUID reportId) {
        UUID adminId = SecurityUtil.getCurrentUserId();
        var report = reportRepo.findById(reportId).orElseThrow();
        report.setResolvedAt(Instant.now());
        report.setResolvedBy(adminId);
        return forumReportMapper.toResponseWithFetch(reportRepo.save(report));
    }
}
