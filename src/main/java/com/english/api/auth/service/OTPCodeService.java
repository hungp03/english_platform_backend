package com.english.api.auth.service;

/**
 * Created by hungpham on 9/24/2025
 */
public interface OTPCodeService {
    String generateOTP(String email);
    boolean validateCode(String k, String v);
    void deleteCode(String key);
}
