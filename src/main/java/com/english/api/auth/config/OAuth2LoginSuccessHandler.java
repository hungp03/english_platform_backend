package com.english.api.auth.config;

import com.english.api.auth.security.CustomUserDetails;
import com.english.api.auth.service.CustomOauth2Service;
import com.english.api.auth.service.JwtService;
import com.english.api.auth.util.CookieUtil;
import com.english.api.user.model.User;
import com.english.api.user.model.UserOAuth2Token;
import com.english.api.user.repository.UserOAuth2TokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

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
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final UserOAuth2TokenRepository tokenRepository;

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
            String avatar = oAuth2User.getAttribute("picture");
            User user = customOauth2Service.processOAuth2User(email, name, socialId, provider, avatar);

            // Store OAuth2 tokens for Google Calendar integration
            storeOAuth2Tokens(oauthToken, user);

            CustomUserDetails userDetails = CustomUserDetails.fromUser(user);

            String accessToken = jwtService.generateAccessToken(userDetails);
            String refreshToken = jwtService.generateRefreshToken(userDetails);

            // Redirect về frontend với token trong URL
            String redirectUrl = String.format("%s/auth/callback/success?access_token=%s&refresh_token=%s",
                    client, accessToken, refreshToken);
            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            response.sendRedirect(client + "/auth/callback/error?isLogin=false");
        }
    }

    private void storeOAuth2Tokens(OAuth2AuthenticationToken authToken, User user) {
        try {
            String registrationId = authToken.getAuthorizedClientRegistrationId();
            OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                    registrationId,
                    authToken.getName()
            );

            if (authorizedClient != null) {
                OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
                OAuth2RefreshToken refreshToken = authorizedClient.getRefreshToken();

                UserOAuth2Token token = tokenRepository.findByUserIdAndProvider(user.getId(), user.getProvider())
                        .orElse(UserOAuth2Token.builder()
                                .user(user)
                                .provider(user.getProvider())
                                .build());

                token.setAccessToken(accessToken.getTokenValue());
                if (refreshToken != null) {
                    token.setRefreshToken(refreshToken.getTokenValue());
                }
                if (accessToken.getExpiresAt() != null) {
                    token.setTokenExpiresAt(OffsetDateTime.ofInstant(accessToken.getExpiresAt(), ZoneOffset.UTC));
                }

                tokenRepository.save(token);
            }
        } catch (Exception e) {
            // Log but don't fail the login process
            // Just means calendar integration won't work for this user
            System.err.println("Error storing OAuth2 tokens for user " + user.getId() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
