package com.english.api.auth.service.impl;

import com.english.api.auth.model.VerificationToken;
import com.english.api.auth.repository.VerificationTokenRepository;
import com.english.api.auth.service.VerificationTokenService;
import com.english.api.common.exception.ResourceInvalidException;
import com.english.api.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Created by hungpham on 9/24/2025
 */
@Service
@RequiredArgsConstructor
public class VerificationTokenServiceImpl implements VerificationTokenService {
    private final VerificationTokenRepository verificationTokenRepository;

    @Override
    public VerificationToken createToken(User user) {
        String tokenValue = UUID.randomUUID().toString();
        VerificationToken token = VerificationToken.builder()
                .token(tokenValue)
                .user(user)
                .expiryDate(Instant.now().plus(Duration.ofMinutes(31)))
                .build();
        return verificationTokenRepository.save(token);
    }

    @Override
    public VerificationToken validateToken(String token) {
        return verificationTokenRepository.findByToken(token)
                .filter(vt -> vt.getExpiryDate().isAfter(Instant.now()))
                .orElseThrow(() -> new ResourceInvalidException("Invalid or expired token"));
    }

    @Override
    @Transactional
    public void purgeExpiredTokens() {
        verificationTokenRepository.deleteAllExpiredSince(Instant.now());
    }

    @Override
    public void delete(VerificationToken vt) {
        verificationTokenRepository.delete(vt);
    }

    @Override
    public VerificationToken findByUser(User user) {
        return verificationTokenRepository.findByUser(user).orElse(null);
    }

    @Override
    public VerificationToken refreshToken(VerificationToken oldToken) {
        // Update token value + expiry date
        oldToken.setToken(UUID.randomUUID().toString());
        oldToken.setExpiryDate(Instant.now().plus(Duration.ofMinutes(31)));
        return verificationTokenRepository.save(oldToken);
    }

}
