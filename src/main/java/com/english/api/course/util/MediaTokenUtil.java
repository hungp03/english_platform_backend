package com.english.api.course.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * Created by hungpham on 10/18/2025
 */
@Slf4j
public class MediaTokenUtil {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static String createSignedToken(Map<String, Object> payload, String secret) {
        try {
            // --- Header ---
            String headerJson = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
            String header = base64UrlEncode(headerJson.getBytes(StandardCharsets.UTF_8));

            // --- Payload ---
            String payloadJson = mapper.writeValueAsString(payload);
            String body = base64UrlEncode(payloadJson.getBytes(StandardCharsets.UTF_8));

            // --- Signature ---
            String signature = signHmacSHA256(header + "." + body, secret);

            return header + "." + body + "." + signature;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate JWT token", e);
        }
    }

    private static String signHmacSHA256(String data, String secret) throws Exception {
        Mac hmac = Mac.getInstance("HmacSHA256");
        hmac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] sig = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return base64UrlEncode(sig);
    }

    private static String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
