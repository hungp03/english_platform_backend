package com.english.api.user.service.impl;

import com.english.api.common.exception.ResourceNotFoundException;
import com.english.api.user.dto.request.CreateInstructorRequest;
import com.english.api.user.dto.request.ReviewInstructorRequest;
import com.english.api.common.dto.PaginationResponse;
import com.english.api.user.dto.response.InstructorRequestListResponse;
import com.english.api.user.dto.response.InstructorRequestListResponse.InstructorRequestItem;
import com.english.api.user.dto.response.InstructorRequestListResponse.UserSimpleResponse;
import com.english.api.user.dto.response.InstructorRequestResponse;
import com.english.api.user.dto.response.UserResponse;
import com.english.api.user.exception.InstructorRequestAlreadyExistsException;
import com.english.api.user.exception.InvalidInstructorRequestException;
import com.english.api.user.mapper.InstructorRequestMapper;
import com.english.api.user.model.InstructorRequest;
import com.english.api.user.model.Role;
import com.english.api.user.model.User;
import com.english.api.user.repository.InstructorRequestRepository;
import com.english.api.user.repository.RoleRepository;
import com.english.api.user.repository.UserRepository;
import com.english.api.user.service.InstructorRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class InstructorRequestServiceImpl implements InstructorRequestService {

    private final InstructorRequestRepository instructorRequestRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final InstructorRequestMapper instructorRequestMapper;

    @Override
    public InstructorRequestResponse createInstructorRequest(UUID userId, CreateInstructorRequest request) {
        // Check if user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Check if user already has an active request
        if (instructorRequestRepository.countActiveRequestsByUserId(userId) > 0) {
            throw new InstructorRequestAlreadyExistsException("User already has an active instructor request");
        }

        // Check if user is already an instructor
        boolean isInstructor = user.getRoles().stream()
                .anyMatch(role -> role.getCode().equals("INSTRUCTOR"));
        if (isInstructor) {
            throw new InvalidInstructorRequestException("User is already an instructor");
        }

        // Create new instructor request
        InstructorRequest instructorRequest = new InstructorRequest(
                user,
                request.bio(),
                request.expertise(),
                request.experienceYears(),
                request.qualification(),
                request.reason()
        );
        instructorRequest.setStatus(InstructorRequest.Status.PENDING);

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
    public InstructorRequestResponse reviewInstructorRequest(UUID requestId, ReviewInstructorRequest reviewRequest, UUID adminId) {
        InstructorRequest request = instructorRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor request not found with id: " + requestId));

        if (request.getStatus() != InstructorRequest.Status.PENDING) {
            throw new InvalidInstructorRequestException("Request is not in pending status");
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found with id: " + adminId));

        // Check if admin has ADMIN role
        boolean isAdmin = admin.getRoles().stream()
                .anyMatch(role -> role.getCode().equals("ADMIN"));
        if (!isAdmin) {
            throw new InvalidInstructorRequestException("Only admin users can review instructor requests");
        }

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
    public InstructorRequestResponse getUserCurrentRequest(UUID userId) {
        InstructorRequest request = instructorRequestRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No instructor request found for user"));

        return instructorRequestMapper.toResponse(request);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canUserSubmitRequest(UUID userId) {
        return instructorRequestRepository.countActiveRequestsByUserId(userId) == 0;
    }

    @Override
    public void deleteRequest(UUID requestId) {
        InstructorRequest request = instructorRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor request not found with id: " + requestId));

        instructorRequestRepository.deleteById(requestId);
        log.info("Deleted instructor request: {}", requestId);
    }
}