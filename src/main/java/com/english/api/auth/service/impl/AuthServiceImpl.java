package com.english.api.auth.service.impl;

import com.english.api.auth.dto.request.AuthRequest;
import com.english.api.auth.dto.response.AuthResponse;
import com.english.api.auth.dto.response.UserLoginResponse;
import com.english.api.auth.security.CustomUserDetails;
import com.english.api.auth.service.AuthService;
import com.english.api.auth.service.JwtService;
import com.english.api.common.exception.AccessDeniedException;
import com.english.api.common.exception.ResourceInvalidException;
import com.english.api.common.exception.ResourceNotFoundException;
import com.english.api.user.model.User;
import com.english.api.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Created by hungpham on 9/23/2025
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final UserService userService;
    private final JwtService jwtService;
    private final RedisTemplate<String, String> redisTemplate;
    @Override
    public AuthResponse login(AuthRequest request) {
        final String identifier = request.identifier();
        User currentUser = userService.findByEmail(identifier);
        checkAccountActive(currentUser);
        checkPasswordExists(currentUser);
        Authentication authentication = authenticate(request.identifier(), request.password());
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);
        UserLoginResponse userLoginResponse = UserLoginResponse.from(currentUser, accessToken);
        return AuthResponse.of(userLoginResponse, accessToken, refreshToken);
    }

    @Override
    public AuthResponse renewToken(String refreshToken) {
        if (refreshToken == null || refreshToken.equals("none")) {
            throw new ResourceNotFoundException("Please sign in first");
        }

        Jwt decodedRefreshToken = jwtService.decode(refreshToken);
        String jti = decodedRefreshToken.getId();
        // parse UUID
        UUID uid = UUID.fromString(decodedRefreshToken.getSubject());

        String blacklistKey = "rt_revoked:" + jti;
        Boolean isBlacklisted = redisTemplate.hasKey(blacklistKey);
        if (isBlacklisted) {
            throw new ResourceInvalidException("Refresh token is revoked");
        }

        // Check expired
        Instant now = Instant.now();
        Instant exp = decodedRefreshToken.getExpiresAt();
        if (exp != null && now.isAfter(exp)) {
            throw new ResourceInvalidException("Refresh token is expired");
        }

        User user = userService.findById(uid);
        CustomUserDetails userDetails = CustomUserDetails.fromUser(user);

        String newAccessToken = jwtService.generateAccessToken(userDetails);
        String newRefreshToken = jwtService.generateRefreshToken(userDetails);

        // Add old jti to blacklist
        if (exp != null) {
            Duration ttl = Duration.between(now, exp);
            redisTemplate.opsForValue().set(blacklistKey, "1", ttl);
        }

        UserLoginResponse userLoginResponse = UserLoginResponse.from(user, newAccessToken);
        return AuthResponse.of(userLoginResponse, newAccessToken, newRefreshToken);
    }


    private void checkAccountActive(User currentUser) {
        if (!currentUser.isActive()) {
            throw new AccessDeniedException("User is locked");
        }
    }

    private void checkPasswordExists(User user) {
        if (user.getPasswordHash() == null || user.getPasswordHash().isEmpty()) {
            throw new BadCredentialsException("Your account has no password.");
        }
    }

    private Authentication authenticate(String username, String password) {
        return authenticationManagerBuilder.getObject().authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
    }
}
