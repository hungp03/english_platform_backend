package com.english.api.evaluation.controller;

import com.english.api.evaluation.dto.request.EvaluationCallbackRequest;
import com.english.api.evaluation.dto.response.AckResponse;
import com.english.api.evaluation.service.EvaluationCallbackService;
import com.english.api.evaluation.util.HmacVerifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.time.Duration;
import java.time.Instant;

@RestController
@RequestMapping("/api/evaluation/callback")
@RequiredArgsConstructor
public class EvaluationCallbackController {

    private final ObjectMapper objectMapper;
    private final EvaluationCallbackService service;

    @Value("${evaluation.webhook.secret:}")
    private String secret;

    @Value("${evaluation.webhook.maxSkewSeconds:300}")
    private long maxSkewSeconds;

    @PostMapping
    public ResponseEntity<AckResponse> receive(HttpServletRequest request) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        final String rawBody = sb.toString();

        String signature = request.getHeader("X-Eval-Signature"); // sha256=xxxx
        String tsHeader = request.getHeader("X-Eval-Timestamp");
        String idempotencyKey = request.getHeader("X-Idempotency-Key");

        if (secret == null || secret.isBlank()) {
            return ResponseEntity.status(500).body(new AckResponse("error", "Webhook secret not configured"));
        }
        if (signature == null || tsHeader == null) {
            return ResponseEntity.badRequest().body(new AckResponse("error", "Missing signature or timestamp"));
        }

        long tsMillis;
        try {
            tsMillis = Long.parseLong(tsHeader);
        } catch (NumberFormatException ex) {
            return ResponseEntity.badRequest().body(new AckResponse("error", "Invalid timestamp"));
        }

        // Check clock skew
        long skew = Math.abs(Instant.now().toEpochMilli() - tsMillis);
        if (skew > Duration.ofSeconds(maxSkewSeconds).toMillis()) {
            return ResponseEntity.status(400).body(new AckResponse("error", "Stale timestamp"));
        }

        boolean ok = HmacVerifier.verifySha256(secret, rawBody, tsHeader, signature);
        if (!ok) {
            return ResponseEntity.status(401).body(new AckResponse("error", "Invalid signature"));
        }

        EvaluationCallbackRequest payload = objectMapper.readValue(rawBody, EvaluationCallbackRequest.class);
        AckResponse resp = service.handleCallback(payload, idempotencyKey, rawBody, tsMillis, true);
        return ResponseEntity.ok(resp);
    }
}
