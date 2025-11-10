package com.english.api.user.service;

import com.english.api.common.dto.PaginationResponse;
import com.english.api.user.dto.request.CreateInstructorRequest;
import com.english.api.user.dto.request.ReviewInstructorRequest;
import com.english.api.user.dto.request.UpdateInstructorRequest;
import com.english.api.user.dto.request.UploadCertificateProofRequest;
import com.english.api.user.dto.response.CertificateProofResponse;
import com.english.api.user.dto.response.InstructorRequestResponse;
import com.english.api.user.model.InstructorRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Instructor Request operations
 * Created by hungpham on 10/29/2025
 */
public interface InstructorService {
    
    InstructorRequestResponse createInstructorRequest(CreateInstructorRequest request);
    
    InstructorRequestResponse getInstructorRequest(UUID requestId);
    
    PaginationResponse getAllInstructorRequests(InstructorRequest.Status status, Pageable pageable);
    
    InstructorRequestResponse reviewInstructorRequest(UUID requestId, ReviewInstructorRequest reviewRequest);
    
    InstructorRequestResponse getUserCurrentRequest();
    
    InstructorRequestResponse getUserRequestById(UUID requestId);
    
    List<InstructorRequestResponse> getUserRequests();
    
    InstructorRequestResponse updatePendingRequest(UUID requestId, UpdateInstructorRequest request);
    
    List<CertificateProofResponse> uploadCertificateProof(UUID requestId, UploadCertificateProofRequest request);
    
    List<CertificateProofResponse> getCertificateProofs(UUID requestId);
    
    void deleteCertificateProof(UUID requestId, UUID proofId);
    
    void deleteCertificateProofByOwner(UUID proofId);
    
    void deleteRequest(UUID requestId);
    
    PaginationResponse getAllInstructors(String search, Pageable pageable);
}
