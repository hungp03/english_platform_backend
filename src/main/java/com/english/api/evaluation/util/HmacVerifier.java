package com.english.api.evaluation.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public final class HmacVerifier {

    private HmacVerifier() {}

    // signature header format: "sha256=<hex>"
    public static boolean verifySha256(String secret, String rawBody, String timestamp, String signatureHeader) {
        if (signatureHeader == null || !signatureHeader.startsWith("sha256=")) return false;
        String provided = signatureHeader.substring("sha256=".length()).trim();
        String data = rawBody + "." + timestamp;
        String expected = hmacHex(secret, data);
        return slowEquals(provided, expected);
    }

    private static String hmacHex(String secret, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] out = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return toHex(out);
        } catch (Exception e) {
            return "";
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static boolean slowEquals(String a, String b) {
        if (a == null || b == null) return false;
        byte[] aBytes = a.getBytes(StandardCharsets.UTF_8);
        byte[] bBytes = b.getBytes(StandardCharsets.UTF_8);
        int diff = aBytes.length ^ bBytes.length;
        for (int i = 0; i < Math.min(aBytes.length, bBytes.length); i++) {
            diff |= aBytes[i] ^ bBytes[i];
        }
        return diff == 0;
    }
}
