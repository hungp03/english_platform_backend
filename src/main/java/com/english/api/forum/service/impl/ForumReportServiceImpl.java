package com.english.api.forum.service.impl;

import com.english.api.auth.util.SecurityUtil;
import com.english.api.common.dto.PaginationResponse;
import com.english.api.forum.dto.request.ForumReportCreateRequest;
import com.english.api.forum.dto.response.ForumReportResponse;
import com.english.api.forum.entity.ForumReport;
import com.english.api.forum.entity.ReportTargetType;
import com.english.api.forum.repo.ForumReportRepository;
import com.english.api.forum.repo.ForumPostRepository;
import com.english.api.forum.repo.ForumThreadRepository;
import com.english.api.user.repository.UserRepository;
import com.english.api.forum.service.ForumReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ForumReportServiceImpl implements ForumReportService {

  private final ForumReportRepository repo;
  private final ForumPostRepository postRepo;
  private final ForumThreadRepository threadRepo;
  private final UserRepository userRepo;

  @Override
  @Transactional
  public ForumReportResponse create(ForumReportCreateRequest req) {
    UUID userId = SecurityUtil.getCurrentUserId();
    var entity = ForumReport.builder()
        .targetType(req.targetType())
        .targetId(req.targetId())
        .userId(userId)
        .reason(req.reason())
        .build();
    entity = repo.save(entity);
    return toDto(entity);
  }

  @Override
  public PaginationResponse list(ReportTargetType type, boolean onlyOpen, Pageable pageable) {
    Page<ForumReport> page = onlyOpen
        ? repo.findByTargetTypeAndResolvedAtIsNull(type, pageable)
        // : repo.findAll(pageable);
        : repo.findByTargetType(type, pageable);
    var mapped = page.getContent().stream().map(this::toDto).toList();
    return PaginationResponse.from(new PageImpl<>(mapped, pageable, page.getTotalElements()), pageable);
  }

  @Override
  @Transactional
  public ForumReportResponse resolve(UUID reportId) {
    UUID adminId = SecurityUtil.getCurrentUserId();
    var r = repo.findById(reportId).orElseThrow();
    r.setResolvedAt(Instant.now());
    r.setResolvedBy(adminId);
    return toDto(repo.save(r));
  }

  private ForumReportResponse toDto(ForumReport r) {
    String preview = null; Boolean targetPublished = null;
    String reporterName = null;
    String reporterEmail = null;
    if (r.getTargetType() == ReportTargetType.POST) {
      var p = postRepo.findById(r.getTargetId()).orElse(null);
      if (p != null) { preview = p.getBodyMd(); targetPublished = p.isPublished(); }
    } else if (r.getTargetType() == ReportTargetType.THREAD) {
      var t = threadRepo.findById(r.getTargetId()).orElse(null);
      if (t != null) { preview = t.getTitle(); targetPublished = !t.isLocked(); }
    }
    if (r.getUserId()!=null) {
      var u = userRepo.findById(r.getUserId()).orElse(null);
      reporterName = u!=null? u.getFullName(): null;
      reporterEmail = u!=null? u.getEmail(): null;
    }
    return new ForumReportResponse(
        r.getId(), r.getTargetType(), r.getTargetId(), r.getUserId(), reporterName,reporterEmail,
        r.getReason(), preview, targetPublished, r.getCreatedAt(), r.getResolvedAt(), r.getResolvedBy()
    );
  }
}
