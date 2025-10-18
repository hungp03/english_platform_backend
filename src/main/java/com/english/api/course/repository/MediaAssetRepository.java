package com.english.api.course.repository;

import com.english.api.course.dto.response.MediaAssetSimpleResponse;
import com.english.api.course.model.MediaAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by hungpham on 10/7/2025
 */
@Repository
public interface MediaAssetRepository extends JpaRepository<MediaAsset, UUID> {
    @Query("""
    SELECT new com.english.api.course.dto.response.MediaAssetSimpleResponse(
        a.owner.id,
        a.url,
        a.meta
    )
    FROM MediaAsset a
    WHERE a.id = :id
      AND LOWER(a.mimeType) LIKE 'video/%'
""")
    Optional<MediaAssetSimpleResponse> findVideoById(@Param("id") UUID id);

}
