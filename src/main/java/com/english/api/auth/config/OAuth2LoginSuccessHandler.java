package com.english.api.auth.config;

import com.english.api.auth.security.CustomUserDetails;
import com.english.api.auth.service.CustomOauth2Service;
import com.english.api.auth.service.JwtService;
import com.english.api.auth.util.CookieUtil;
import com.english.api.user.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Created by hungpham on 9/25/2025
 */
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    @Value("${app.client-url}")
    private String client;

    @Value("${jwt.expiration.access-token}")
    private long accessTokenExpiration;   // second

    @Value("${jwt.expiration.refresh-token}")
    private long refreshTokenExpiration;  // second

    private final JwtService jwtService;
    private final CustomOauth2Service customOauth2Service;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        try {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            OAuth2User oAuth2User = oauthToken.getPrincipal();

            String provider = oauthToken.getAuthorizedClientRegistrationId().toUpperCase();
            String email = oAuth2User.getAttribute("email");
            String name = oAuth2User.getAttribute("name");
            String socialId = oAuth2User.getAttribute("sub");
            if (socialId == null) {
                socialId = oAuth2User.getAttribute("id");
            }

            User user = customOauth2Service.processOAuth2User(email, name, socialId, provider);
            CustomUserDetails userDetails = CustomUserDetails.fromUser(user);

            String accessToken = jwtService.generateAccessToken(userDetails);
            String refreshToken = jwtService.generateRefreshToken(userDetails);

            ResponseCookie accessTokenCookie = CookieUtil.buildCookie("access_token", accessToken, accessTokenExpiration);
            ResponseCookie refreshTokenCookie = CookieUtil.buildCookie("refresh_token", refreshToken, refreshTokenExpiration);

            response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
            response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

            // Redirect to FE
            response.sendRedirect(client + "/authentication/success");

        } catch (Exception e) {
            response.sendRedirect(client + "/authentication/error?isLogin=false");
        }
    }
}
