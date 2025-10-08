package com.english.api.course.repository;

import com.english.api.course.model.MediaAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Created by hungpham on 10/7/2025
 */
@Repository
public interface MediaAssetRepository extends JpaRepository<MediaAsset, UUID> {}
