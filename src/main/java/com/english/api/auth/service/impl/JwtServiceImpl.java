package com.english.api.auth.service.impl;

import com.english.api.auth.security.CustomUserDetails;
import com.english.api.auth.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Created by hungpham on 9/23/2025
 */
@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    @Value("${jwt.expiration.access-token}")
    private Integer ACCESSTOKEN_EXP;

    @Value("${jwt.expiration.refresh-token}")
    private Integer REFRESHTOKEN_EXP;

    @Override
    public Jwt decode(String token) {
        return jwtDecoder.decode(token);
    }

    @Override
    public String generateAccessToken(CustomUserDetails userDetails) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(ACCESSTOKEN_EXP, ChronoUnit.SECONDS))
                .subject(userDetails.user().getId().toString())
                .id(UUID.randomUUID().toString()) // jti
                .claim("username", userDetails.getUsername())
                .claim("authorities", userDetails.user().getRoles().stream()
                        .map(role -> "ROLE_" + role.getCode())
                        .toList()
                )
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }


    @Override
    public String generateRefreshToken(CustomUserDetails userDetails) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(30, ChronoUnit.DAYS))
                .subject(userDetails.user().getId().toString())
                .id(UUID.randomUUID().toString())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

}
