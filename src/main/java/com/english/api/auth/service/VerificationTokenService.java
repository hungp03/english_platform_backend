package com.english.api.auth.service;

import com.english.api.auth.model.VerificationToken;
import com.english.api.user.model.User;

/**
 * Created by hungpham on 9/24/2025
 */
public interface VerificationTokenService {
    VerificationToken createToken(User user);
    VerificationToken validateToken(String token);
    void purgeExpiredTokens();
    void delete(VerificationToken vt);

    VerificationToken findByUser(User user);

    VerificationToken refreshToken(VerificationToken oldToken);
}
