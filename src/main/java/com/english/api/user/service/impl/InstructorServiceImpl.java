package com.english.api.user.service.impl;

import com.english.api.auth.util.SecurityUtil;
import com.english.api.common.exception.ResourceAlreadyExistsException;
import com.english.api.common.exception.ResourceInvalidException;
import com.english.api.common.exception.ResourceNotFoundException;
import com.english.api.user.dto.request.CreateInstructorRequest;
import com.english.api.user.dto.request.ReviewInstructorRequest;
import com.english.api.user.dto.request.UpdateInstructorRequest;
import com.english.api.common.dto.PaginationResponse;
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
        InstructorRequest request = instructorRequestRepository.findById(requestId)
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
        Page<InstructorRequestListResponse.InstructorRequestItem> itemPage =
            new org.springframework.data.domain.PageImpl<>(items, requests.getPageable(), requests.getTotalElements());

        return PaginationResponse.from(itemPage, pageable);
    }

    @Override
    public InstructorRequestResponse reviewInstructorRequest(UUID requestId, ReviewInstructorRequest reviewRequest) {
        InstructorRequest request = instructorRequestRepository.findById(requestId)
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
    public InstructorRequestResponse updatePendingRequest(UUID requestId, UpdateInstructorRequest request) {
        InstructorRequest instructorRequest = instructorRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor request not found with id: " + requestId));

        if (instructorRequest.getStatus() != InstructorRequest.Status.PENDING) {
            throw new ResourceInvalidException("Only pending requests can be updated");
        }

        UUID currentUserId = SecurityUtil.getCurrentUserId();
        if (!instructorRequest.getUser().getId().equals(currentUserId)) {
            throw new ResourceInvalidException("You can only update your own requests");
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
    public void deleteRequest(UUID requestId) {
        if (!instructorRequestRepository.existsById(requestId)) {
            throw new ResourceNotFoundException("Instructor request not found with id: " + requestId);
        }
        instructorRequestRepository.deleteById(requestId);
        log.info("Deleted instructor request: {}", requestId);
    }
}
