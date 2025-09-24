package com.english.api.auth.controller;

import com.english.api.auth.dto.request.AuthRequest;
import com.english.api.auth.dto.response.AuthResponse;
import com.english.api.auth.dto.response.UserLoginResponse;
import com.english.api.auth.service.AuthService;
import com.english.api.auth.util.CookieUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Created by hungpham on 9/23/2025
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @Value("${jwt.expiration.access-token}")
    private long accessTokenExpiration;   // second

    @Value("${jwt.expiration.refresh-token}")
    private long refreshTokenExpiration;  // second

    @PostMapping("login")
    public ResponseEntity<UserLoginResponse> login(@Valid @RequestBody AuthRequest authRequest) {
        AuthResponse authResponse = authService.login(authRequest);

        ResponseCookie accessTokenCookie = CookieUtil.buildCookie("access_token", authResponse.accessToken(), accessTokenExpiration);
        ResponseCookie refreshTokenCookie = CookieUtil.buildCookie("refresh_token", authResponse.refreshToken(), refreshTokenExpiration);

        return ResponseEntity.ok()
                .header("Set-Cookie", accessTokenCookie.toString())
                .header("Set-Cookie", refreshTokenCookie.toString())
                .body(authResponse.user());
    }


    @PostMapping("refresh")
    public ResponseEntity<UserLoginResponse> refresh(@CookieValue(name = "refresh_token", defaultValue = "none") String refreshToken) {
        AuthResponse authResponse = authService.renewToken(refreshToken);

        ResponseCookie accessTokenCookie = CookieUtil.buildCookie("access_token", authResponse.accessToken(), accessTokenExpiration);
        ResponseCookie refreshTokenCookie = CookieUtil.buildCookie("refresh_token", authResponse.refreshToken(), refreshTokenExpiration);

        return ResponseEntity.ok()
                .header("Set-Cookie", accessTokenCookie.toString())
                .header("Set-Cookie", refreshTokenCookie.toString())
                .body(authResponse.user());
    }
}
