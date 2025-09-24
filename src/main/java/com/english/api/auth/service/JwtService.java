package com.english.api.auth.service;

import com.english.api.auth.security.CustomUserDetails;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Created by hungpham on 9/23/2025
 */
public interface JwtService {
    Jwt decode(String token);
    String generateAccessToken(CustomUserDetails userDetails);
    String generateRefreshToken(CustomUserDetails userDetails);
}
