package com.english.api.auth.dto.response;

/**
 * Created by hungpham on 9/22/2025
 */
public record AuthResponse(UserLoginResponse user, String accessToken, String refreshToken) {
    public static AuthResponse of(UserLoginResponse user, String accessToken, String refreshToken) {
        return new AuthResponse(user, accessToken, refreshToken);
    }
}
