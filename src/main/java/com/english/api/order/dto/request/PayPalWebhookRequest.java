package com.english.api.order.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * PayPal webhook event structure
 */
public record PayPalWebhookRequest(
        @JsonProperty("id")
        String eventId,
        
        @JsonProperty("event_type")
        String eventType,
        
        @JsonProperty("resource")
        Map<String, Object> resource,
        
        @JsonProperty("create_time")
        String createTime,
        
        @JsonProperty("summary")
        String summary
) {}
