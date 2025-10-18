package com.english.api.course.dto.response;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.UUID;

/**
 * Created by hungpham on 10/18/2025
 */
public record MediaAssetSimpleResponse (
    UUID ownerId,
    String url,
    JsonNode meta
){}
