package com.english.api.auth.service;

import com.english.api.auth.dto.request.AuthRequest;
import com.english.api.auth.dto.request.LinkGoogleAccountRequest;
import com.english.api.auth.dto.request.RegisterRequest;
import com.english.api.auth.dto.request.ResetPasswordRequest;
import com.english.api.auth.dto.response.AuthResponse;
import com.english.api.auth.dto.response.LinkAccountResponse;
import com.english.api.auth.dto.response.OtpVerificationResponse;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by hungpham on 9/23/2025
 */
public interface AuthService {
    @Transactional
    String register(RegisterRequest request);

    @Transactional
    void verifyAccount(String token);

    AuthResponse login(AuthRequest request);
    AuthResponse renewToken(String refreshToken);

    void logout(String accessToken, String refreshToken);

    void logoutAll(String accessToken);

    void forgotPassword(String email);

    @Transactional
    OtpVerificationResponse verifyOtp(String email, String inputOtp);

    void resetPassword(ResetPasswordRequest request);

    @Transactional
    LinkAccountResponse linkGoogleAccount(LinkGoogleAccountRequest request);

    @Transactional
    LinkAccountResponse unlinkGoogleAccount();
}
