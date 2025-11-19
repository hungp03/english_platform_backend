package com.english.api.auth.controller;

import com.english.api.auth.dto.request.*;
import com.english.api.auth.dto.response.AuthResponse;
import com.english.api.auth.dto.response.LinkAccountResponse;
import com.english.api.auth.dto.response.OtpVerificationResponse;
import com.english.api.auth.dto.response.UserLoginResponse;
import com.english.api.auth.service.AuthService;
import com.english.api.auth.util.CookieUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @GetMapping("/verify-register")
    public ResponseEntity<?> verifyAccount(@RequestParam("token") String token) {
        authService.verifyAccount(token);
        return ResponseEntity.ok("Account verified successfully.");
    }

    @PostMapping("login")
    public ResponseEntity<UserLoginResponse> login(@Valid @RequestBody AuthRequest authRequest) {
        AuthResponse authResponse = authService.login(authRequest);

        ResponseCookie accessTokenCookie = CookieUtil.buildCookie(
                "access_token", authResponse.accessToken(), accessTokenExpiration
        );
        ResponseCookie refreshTokenCookie = CookieUtil.buildCookie(
                "refresh_token", authResponse.refreshToken(), refreshTokenExpiration
        );

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        headers.add(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        return ResponseEntity.ok()
                .headers(headers)
                .body(authResponse.user());
    }



    @PostMapping("refresh")
    public ResponseEntity<UserLoginResponse> refresh(@CookieValue(name = "refresh_token", defaultValue = "none") String refreshToken) {
        System.out.println(refreshToken);
        AuthResponse authResponse = authService.renewToken(refreshToken);

        ResponseCookie accessTokenCookie = CookieUtil.buildCookie("access_token", authResponse.accessToken(), accessTokenExpiration);
        ResponseCookie refreshTokenCookie = CookieUtil.buildCookie("refresh_token", authResponse.refreshToken(), refreshTokenExpiration);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        headers.add(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        return ResponseEntity.ok()
                .headers(headers)
                .body(authResponse.user());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "access_token", defaultValue = "none") String accessToken,
            @CookieValue(name = "refresh_token", defaultValue = "none") String refreshToken) {

        authService.logout(accessToken, refreshToken);

        // Clear cookie (set maxAge=0)
        ResponseCookie clearAccessToken = CookieUtil.buildCookie("access_token", "", 0);
        ResponseCookie clearRefreshToken = CookieUtil.buildCookie("refresh_token", "", 0);

        return ResponseEntity.ok()
                .header("Set-Cookie", clearAccessToken.toString())
                .header("Set-Cookie", clearRefreshToken.toString())
                .build();
    }

    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(
            @CookieValue(name = "access_token", defaultValue = "none") String accessToken) {

        authService.logoutAll(accessToken);

        // Clear cookie (set maxAge=0)
        ResponseCookie clearAccessToken = CookieUtil.buildCookie("access_token", "", 0);
        ResponseCookie clearRefreshToken = CookieUtil.buildCookie("refresh_token", "", 0);

        return ResponseEntity.ok()
                .header("Set-Cookie", clearAccessToken.toString())
                .header("Set-Cookie", clearRefreshToken.toString())
                .build();
    }

    @PostMapping("forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        this.authService.forgotPassword(forgotPasswordRequest.email());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<OtpVerificationResponse> verifyOtp(@Valid @RequestBody OTPResetRequest request) {
        return ResponseEntity.ok(this.authService.verifyOtp(request.email(), request.otp()));
    }

    @PostMapping("reset-password")
    public ResponseEntity<Void> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        this.authService.resetPassword(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/link-google-account")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LinkAccountResponse> linkGoogleAccount(
            @Valid @RequestBody LinkGoogleAccountRequest request) {
        return ResponseEntity.ok(authService.linkGoogleAccount(request));
    }

    @PostMapping("/unlink-google-account")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LinkAccountResponse> unlinkGoogleAccount() {
        return ResponseEntity.ok(authService.unlinkGoogleAccount());
    }
}
