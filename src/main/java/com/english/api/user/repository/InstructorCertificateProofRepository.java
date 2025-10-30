package com.english.api.user.repository;

import com.english.api.user.model.InstructorCertificateProof;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InstructorCertificateProofRepository extends JpaRepository<InstructorCertificateProof, UUID> {

    @Query("SELECT cp FROM InstructorCertificateProof cp WHERE cp.instructorRequest.id = :requestId ORDER BY cp.uploadedAt ASC")
    List<InstructorCertificateProof> findByRequestId(@Param("requestId") UUID requestId);

    @Query("SELECT COUNT(cp) FROM InstructorCertificateProof cp WHERE cp.instructorRequest.id = :requestId")
    int countByRequestId(@Param("requestId") UUID requestId);

    void deleteByInstructorRequestId(UUID requestId);
}
