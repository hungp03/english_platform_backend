package com.english.api.auth.service;

import com.english.api.auth.dto.request.AuthRequest;
import com.english.api.auth.dto.response.AuthResponse;

/**
 * Created by hungpham on 9/23/2025
 */
public interface AuthService {
    AuthResponse login(AuthRequest request);
    AuthResponse renewToken(String refreshToken);
}
