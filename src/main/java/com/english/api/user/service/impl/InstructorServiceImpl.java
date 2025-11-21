package com.english.api.user.service.impl;

import com.english.api.auth.util.SecurityUtil;
import com.english.api.common.exception.ResourceAlreadyExistsException;
import com.english.api.common.exception.ResourceInvalidException;
import com.english.api.common.exception.ResourceNotFoundException;
import com.english.api.user.dto.request.CreateInstructorRequest;
import com.english.api.user.dto.request.ReviewInstructorRequest;
import com.english.api.user.dto.request.UpdateInstructorRequest;
import com.english.api.user.dto.request.UploadCertificateProofRequest;
import com.english.api.common.dto.PaginationResponse;
import com.english.api.user.dto.response.CertificateProofResponse;
import com.english.api.user.dto.response.InstructorBasicInfoResponse;
import com.english.api.user.mapper.CertificateProofMapper;
import com.english.api.user.model.InstructorCertificateProof;
import com.english.api.user.repository.InstructorCertificateProofRepository;
import com.english.api.user.dto.response.InstructorRequestListResponse;
import com.english.api.user.dto.response.InstructorRequestResponse;
import com.english.api.user.mapper.InstructorRequestMapper;
import com.english.api.user.model.InstructorRequest;
import com.english.api.user.model.InstructorProfile;
import com.english.api.user.model.Role;
import com.english.api.user.model.User;
import com.english.api.user.repository.InstructorProfileRepository;
import com.english.api.user.repository.InstructorRequestRepository;
import com.english.api.user.repository.RoleRepository;
import com.english.api.user.repository.UserRepository;
import com.english.api.user.service.InstructorService;
import com.english.api.common.service.MediaService;
import com.english.api.mail.service.MailService;
import com.english.api.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Service implementation for Instructor Request operations
 * Created by hungpham on 10/29/2025
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class InstructorServiceImpl implements InstructorService {

    private final InstructorRequestRepository instructorRequestRepository;
    private final InstructorProfileRepository instructorProfileRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final InstructorRequestMapper instructorRequestMapper;
    private final InstructorCertificateProofRepository certificateProofRepository;
    private final CertificateProofMapper certificateProofMapper;
    private final MediaService mediaService;
    private final MailService mailService;
    private final NotificationService notificationService;

    @Override
    public InstructorRequestResponse createInstructorRequest(CreateInstructorRequest request) {
        UUID userId = SecurityUtil.getCurrentUserId();
        // Check if user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Check if user already has a PENDING request
        if (instructorRequestRepository.countPendingRequestsByUserId(userId) > 0) {
            throw new ResourceAlreadyExistsException("User already has a pending instructor request");
        }

        // Check if user is already an instructor
        boolean isInstructor = user.getRoles().stream()
                .anyMatch(role -> role.getCode().equals("INSTRUCTOR"));
        if (isInstructor) {
            throw new ResourceInvalidException("User is already an instructor");
        }

        // Create new instructor request using builder pattern
        InstructorRequest instructorRequest = InstructorRequest.builder()
                .user(user)
                .bio(request.bio())
                .expertise(request.expertise())
                .experienceYears(request.experienceYears())
                .qualification(request.qualification())
                .reason(request.reason())
                .status(InstructorRequest.Status.PENDING)
                .build();

        InstructorRequest savedRequest = instructorRequestRepository.save(instructorRequest);
        log.info("Created instructor request for user: {}", user.getEmail());

        return instructorRequestMapper.toResponse(savedRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public InstructorRequestResponse getInstructorRequest(UUID requestId) {
        InstructorRequest request = instructorRequestRepository.findByIdWithDetails(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor request not found with id: " + requestId));

        return instructorRequestMapper.toResponse(request);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse getAllInstructorRequests(InstructorRequest.Status status, Pageable pageable) {
        Page<InstructorRequest> requests;
        if (status != null) {
            requests = instructorRequestRepository.findByStatusOrderByRequestedAtDesc(status, pageable);
        } else {
            requests = instructorRequestRepository.findAllRequests(pageable);
        }

        // Convert to DTO items
        List<InstructorRequestListResponse.InstructorRequestItem> items = requests.getContent().stream()
                .map(instructorRequestMapper::toItem)
                .toList();

        // Create page of DTO items
        Page<InstructorRequestListResponse.InstructorRequestItem> itemPage = new org.springframework.data.domain.PageImpl<>(
                items, requests.getPageable(), requests.getTotalElements());

        return PaginationResponse.from(itemPage, pageable);
    }

    @Override
    @Transactional
    public InstructorRequestResponse reviewInstructorRequest(UUID requestId, ReviewInstructorRequest reviewRequest) {
        InstructorRequest request = instructorRequestRepository.findByIdWithDetails(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor request not found with id: " + requestId));

        if (request.getStatus() != InstructorRequest.Status.PENDING) {
            throw new ResourceInvalidException("Request is not in pending status");
        }
        UUID adminId = SecurityUtil.getCurrentUserId();
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found with id: " + adminId));
        // Update request
        request.setReviewedAt(Instant.now());
        request.setReviewedBy(admin);
        request.setAdminNotes(reviewRequest.adminNotes());
        if (reviewRequest.action() == ReviewInstructorRequest.ApprovalAction.APPROVE) {
            request.setStatus(InstructorRequest.Status.APPROVED);
            // Add INSTRUCTOR role to the user
            User requestUser = request.getUser();
            Role instructorRole = roleRepository.findByCode("INSTRUCTOR")
                    .orElseThrow(() -> new ResourceNotFoundException("INSTRUCTOR role not found"));
            if (!requestUser.getRoles().contains(instructorRole)) {
                requestUser.getRoles().add(instructorRole);
                userRepository.save(requestUser);
                log.info("Added INSTRUCTOR role to user: {}", requestUser.getEmail());
            }
            // Create or update instructor profile
            InstructorProfile instructorProfile = instructorProfileRepository.findByUserId(requestUser.getId())
                    .orElse(InstructorProfile.builder()
                            .user(requestUser)
                            .build());

            instructorProfile.setBio(request.getBio());
            instructorProfile.setExpertise(request.getExpertise());
            instructorProfile.setExperienceYears(request.getExperienceYears());
            instructorProfile.setQualification(request.getQualification());

            instructorProfileRepository.save(instructorProfile);
            log.info("Created/updated instructor profile for user: {}", requestUser.getEmail());
            log.info("Approved instructor request for user: {}", requestUser.getEmail());
        } else {
            request.setStatus(InstructorRequest.Status.REJECTED);
            log.info("Rejected instructor request for user: {}", request.getUser().getEmail());
        }

        InstructorRequest updatedRequest = instructorRequestRepository.save(request);

        // Send email notification to user
        User requestUser = request.getUser();
        boolean isApproved = reviewRequest.action() == ReviewInstructorRequest.ApprovalAction.APPROVE;
        if (isApproved) {
            notificationService.sendNotification(
                    requestUser.getId(),
                    "Yêu cầu giảng viên được chấp nhận",
                    "Chúc mừng! Yêu cầu giảng viên của bạn đã được chấp nhận. Giờ bạn có thể tạo khóa học mới.");
        } else {
            notificationService.sendNotification(
                    requestUser.getId(),
                    "Yêu cầu giảng viên đã bị từ chối",
                    "Yêu cầu giảng viên của bạn đã được đánh giá. " +
                            (reviewRequest.adminNotes() != null
                                    ? "Người quản trị ghi chú: " + reviewRequest.adminNotes()
                                    : ""));
        }
        String userName = requestUser.getFullName() != null ? requestUser.getFullName() : requestUser.getEmail();
        mailService.sendInstructorRequestReviewEmail(
                requestUser.getEmail(),
                userName,
                isApproved,
                reviewRequest.adminNotes());

        return instructorRequestMapper.toResponse(updatedRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public InstructorRequestResponse getUserCurrentRequest() {
        UUID userId = SecurityUtil.getCurrentUserId();
        InstructorRequest request = instructorRequestRepository.findLatestByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No instructor request found for user"));
        return instructorRequestMapper.toResponse(request);
    }

    @Override
    @Transactional(readOnly = true)
    public InstructorRequestResponse getUserRequestById(UUID requestId) {
        UUID userId = SecurityUtil.getCurrentUserId();
        InstructorRequest request = instructorRequestRepository.findByIdAndUserId(requestId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Instructor request not found with id: " + requestId + " for current user"));
        return instructorRequestMapper.toResponse(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InstructorRequestResponse> getUserRequests() {
        UUID userId = SecurityUtil.getCurrentUserId();
        List<InstructorRequest> requests = instructorRequestRepository.findByUserId(userId);
        return requests.stream()
                .map(instructorRequestMapper::toResponse)
                .toList();
    }

    @Override
    public InstructorRequestResponse updatePendingRequest(UUID requestId, UpdateInstructorRequest request) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        InstructorRequest instructorRequest = instructorRequestRepository.findByIdAndUserId(requestId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Instructor request not found or you don't have permission"));

        if (instructorRequest.getStatus() != InstructorRequest.Status.PENDING) {
            throw new ResourceInvalidException("Only pending requests can be updated");
        }

        // Update fields if provided
        if (request.bio() != null) {
            instructorRequest.setBio(request.bio());
        }
        if (request.expertise() != null) {
            instructorRequest.setExpertise(request.expertise());
        }
        if (request.experienceYears() != null) {
            instructorRequest.setExperienceYears(request.experienceYears());
        }
        if (request.qualification() != null) {
            instructorRequest.setQualification(request.qualification());
        }
        if (request.reason() != null) {
            instructorRequest.setReason(request.reason());
        }

        InstructorRequest updatedRequest = instructorRequestRepository.save(instructorRequest);
        log.info("Updated instructor request: {}", requestId);

        return instructorRequestMapper.toResponse(updatedRequest);
    }

    @Override
    public List<CertificateProofResponse> uploadCertificateProof(UUID requestId,
            UploadCertificateProofRequest request) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        InstructorRequest instructorRequest = instructorRequestRepository.findByIdAndUserId(requestId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Instructor request not found or you don't have permission"));

        if (instructorRequest.getStatus() != InstructorRequest.Status.PENDING) {
            throw new ResourceInvalidException("Can only upload proofs for pending requests");
        }

        // Batch create certificate proofs for all file URLs
        List<InstructorCertificateProof> proofs = request.fileUrls().stream()
                .map(fileUrl -> InstructorCertificateProof.builder()
                        .instructorRequest(instructorRequest)
                        .fileUrl(fileUrl)
                        .build())
                .toList();

        List<InstructorCertificateProof> savedProofs = certificateProofRepository.saveAll(proofs);
        log.info("Uploaded {} certificate proofs for request: {}", savedProofs.size(), requestId);

        return savedProofs.stream()
                .map(certificateProofMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CertificateProofResponse> getCertificateProofs(UUID requestId) {
        if (!instructorRequestRepository.existsById(requestId)) {
            throw new ResourceNotFoundException("Instructor request not found with id: " + requestId);
        }
        List<InstructorCertificateProof> proofs = certificateProofRepository.findByRequestId(requestId);
        return proofs.stream()
                .map(certificateProofMapper::toResponse)
                .toList();
    }

    @Override
    public void deleteCertificateProof(UUID requestId, UUID proofId) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        InstructorRequest instructorRequest = instructorRequestRepository.findByIdAndUserId(requestId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Instructor request not found or you don't have permission"));

        if (instructorRequest.getStatus() != InstructorRequest.Status.PENDING) {
            throw new ResourceInvalidException("Can only delete proofs from pending requests");
        }

        InstructorCertificateProof proof = certificateProofRepository.findById(proofId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate proof not found with id: " + proofId));

        if (!proof.getInstructorRequest().getId().equals(requestId)) {
            throw new ResourceInvalidException("Proof does not belong to the specified request");
        }

        // Delete file from storage
        String fileUrl = proof.getFileUrl();
        try {
            mediaService.deleteFileByUrl(fileUrl);
            log.info("Deleted file from storage: {}", fileUrl);
        } catch (Exception e) {
            log.warn("Failed to delete file from storage: {}", fileUrl, e);
            // Continue with database deletion even if file deletion fails
        }

        // Remove from parent collection to properly trigger orphan removal
        instructorRequest.getCertificateProofs().remove(proof);
        certificateProofRepository.delete(proof);
        log.info("Deleted certificate proof: {} from request: {}", proofId, requestId);
    }

    @Override
    public void deleteCertificateProofByOwner(UUID proofId) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();

        InstructorCertificateProof proof = certificateProofRepository.findByIdWithRequest(proofId)
                .orElseThrow(() -> new ResourceNotFoundException("Certificate proof not found with id: " + proofId));

        if (!proof.getInstructorRequest().getUser().getId().equals(currentUserId)) {
            throw new ResourceInvalidException("You don't have permission to delete this certificate proof");
        }

        String fileUrl = proof.getFileUrl();
        try {
            mediaService.deleteFileByUrl(fileUrl);
            log.info("Deleted file from storage: {}", fileUrl);
        } catch (Exception e) {
            log.warn("Failed to delete file from storage: {}", fileUrl, e);
        }

        // Remove from parent collection to properly trigger orphan removal
        InstructorRequest instructorRequest = proof.getInstructorRequest();
        instructorRequest.getCertificateProofs().remove(proof);
        certificateProofRepository.delete(proof);
        log.info("Deleted certificate proof: {} by owner", proofId);
    }

    @Override
    public void deleteRequest(UUID requestId) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();

        if (!instructorRequestRepository.existsByIdAndUserId(requestId, currentUserId)) {
            throw new ResourceNotFoundException("Instructor request not found or you don't have permission");
        }

        // Delete all certificate proof files from storage
        List<InstructorCertificateProof> proofs = certificateProofRepository.findByRequestId(requestId);
        for (InstructorCertificateProof proof : proofs) {
            String fileUrl = proof.getFileUrl();
            try {
                mediaService.deleteFileByUrl(fileUrl);
                log.info("Deleted file from storage: {}", fileUrl);
            } catch (Exception e) {
                log.warn("Failed to delete file from storage: {}", fileUrl, e);
            }
        }

        instructorRequestRepository.deleteById(requestId);
        log.info("Deleted instructor request: {} by owner", requestId);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse getAllInstructors(String search, Pageable pageable) {
        Page<InstructorBasicInfoResponse> instructors;
        if (search == null || search.trim().isEmpty()) {
            instructors = instructorProfileRepository.findAllBasicInfo(pageable);
        } else {
            instructors = instructorProfileRepository.findAllBasicInfoWithSearch(search.trim(), pageable);
        }
        return PaginationResponse.from(instructors, pageable);
    }
}