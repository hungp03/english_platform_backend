package com.english.api.user.mapper;

import com.english.api.user.dto.response.CertificateProofResponse;
import com.english.api.user.model.InstructorCertificateProof;
import org.mapstruct.Mapper;

/**
 * Mapper for InstructorCertificateProof
 * Created by hungpham on 10/30/2025
 */
@Mapper(componentModel = "spring")
public interface CertificateProofMapper {

    CertificateProofResponse toResponse(InstructorCertificateProof proof);
}
