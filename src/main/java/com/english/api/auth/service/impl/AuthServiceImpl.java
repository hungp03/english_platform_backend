package com.english.api.auth.service.impl;

import com.english.api.auth.dto.request.AuthRequest;
import com.english.api.auth.dto.request.RegisterRequest;
import com.english.api.auth.dto.request.ResetPasswordRequest;
import com.english.api.auth.dto.response.AuthResponse;
import com.english.api.auth.dto.response.OtpVerificationResponse;
import com.english.api.auth.dto.response.UserLoginResponse;
import com.english.api.auth.model.VerificationToken;
import com.english.api.auth.security.CustomUserDetails;
import com.english.api.auth.service.AuthService;
import com.english.api.auth.service.JwtService;
import com.english.api.auth.service.OTPCodeService;
import com.english.api.auth.service.VerificationTokenService;
import com.english.api.common.exception.AccessDeniedException;
import com.english.api.common.exception.ResourceAlreadyExistsException;
import com.english.api.common.exception.ResourceInvalidException;
import com.english.api.common.exception.ResourceNotFoundException;
import com.english.api.mail.service.MailService;
import com.english.api.user.model.Role;
import com.english.api.user.model.User;
import com.english.api.user.repository.RoleRepository;
import com.english.api.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final UserService userService;
    private final JwtService jwtService;
    private final RedisTemplate<String, String> redisTemplate;
    private final VerificationTokenService verificationTokenService;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final OTPCodeService otpCodeService;

    @Transactional
    @Override
    public String register(RegisterRequest request) {
        if (!Objects.equals(request.password(), request.confirmPassword())) {
            throw new ResourceInvalidException("Passwords do not match.");
        }

        Optional<User> existingUserOpt = userService.findOptionalByEmail(request.email());

        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();

            if (existingUser.isEmailVerified()) {
                throw new ResourceAlreadyExistsException("Email already exists.");
            }

            VerificationToken currentToken = verificationTokenService.findByUser(existingUser);
            if (currentToken != null) {
                Duration age = Duration.between(currentToken.getCreatedAt(), Instant.now());
                if (age.compareTo(Duration.ofMinutes(30)) < 0) {
                    return "Your email is not verified yet. Please check your mailbox.";
                } else {
                    VerificationToken newToken = verificationTokenService.refreshToken(currentToken);
                    mailService.sendRegisterVerificationEmail(
                            existingUser.getEmail(),
                            newToken.getToken(),
                            "register"
                    );
                    return "A new verification link has been sent to your email. Please check your mailbox.";
                }
            }

            // If don't have a token yet → create a new one
            VerificationToken token = verificationTokenService.createToken(existingUser);
            mailService.sendRegisterVerificationEmail(existingUser.getEmail(), token.getToken(), "register");
            return "Your email is not verified yet. Please check your mailbox.";
        }

        // Create new user
        User user = User.builder()
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .provider("local")
                .isActive(false)
                .providerUid(request.email())
                .emailVerified(false)
                .build();

        userService.save(user);

        VerificationToken token = verificationTokenService.createToken(user);
        mailService.sendRegisterVerificationEmail(request.email(), token.getToken(), "register");

        return "Registration successful. Please check your email to verify your account.";
    }


    @Transactional
    @Override
    public void verifyAccount(String token) {
        VerificationToken vt = verificationTokenService.validateToken(token);

        User user = vt.getUser();
        if (user.isEmailVerified()) {
            throw new ResourceAlreadyExistsException("Your account has already been verified.");
        }

        user.setEmailVerified(true);
        user.setActive(true);

        Role userRole = roleRepository.findByCode("USER")
                .orElseThrow(() -> new ResourceNotFoundException("Role USER not found"));
        user.getRoles().add(userRole);

        userService.save(user);
        verificationTokenService.delete(vt);
    }

    @Override
    public AuthResponse login(AuthRequest request) {
        // Find user by email/identifier
        final String identifier = request.identifier();
        User currentUser = userService.findByEmail(identifier);

        // Validate account state
        checkAccountActive(currentUser);
        checkPasswordExists(currentUser);

        // Authenticate credentials
        Authentication authentication = authenticate(request.identifier(), request.password());
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // Generate access/refresh tokens
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // Store refresh token in Redis (whitelist)
        storeRefreshToken(currentUser.getId(), refreshToken);

        // Build response
        UserLoginResponse userLoginResponse = UserLoginResponse.from(currentUser);
        return AuthResponse.of(userLoginResponse, accessToken, refreshToken);
    }

    @Override
    public AuthResponse renewToken(String refreshToken) {
        if (refreshToken == null || refreshToken.equals("none")) {
            throw new ResourceNotFoundException("Please sign in first");
        }

        Jwt decoded = jwtService.decode(refreshToken);
        UUID uid = UUID.fromString(decoded.getSubject());
        String jti = decoded.getId();

        // Check expiration
        Instant now = Instant.now();
        Instant exp = decoded.getExpiresAt();
        if (exp == null || now.isAfter(exp)) {
            throw new ResourceInvalidException("Refresh token is expired");
        }

        // Check if refresh token exists in Redis (whitelist validation)
        String key = buildRtKey(uid, jti);
        if (!redisTemplate.hasKey(key)) {
            throw new ResourceInvalidException("Refresh token is invalid or revoked");
        }

        // Get user from database
        User user = userService.findById(uid);
        CustomUserDetails userDetails = CustomUserDetails.fromUser(user);

        // Generate new tokens
        String newAccessToken = jwtService.generateAccessToken(userDetails);
        String newRefreshToken = jwtService.generateRefreshToken(userDetails);

        // Remove old refresh token and store new one
        redisTemplate.delete(key);
        storeRefreshToken(uid, newRefreshToken);

        // Build response
        UserLoginResponse userLoginResponse = UserLoginResponse.from(user);
        return AuthResponse.of(userLoginResponse, newAccessToken, newRefreshToken);
    }

    @Override
    public void logout(String accessToken, String refreshToken) {
        if ((accessToken == null || accessToken.equals("none")) &&
                (refreshToken == null || refreshToken.equals("none"))) {
            throw new ResourceInvalidException("Please sign in first");
        }

        if (accessToken != null && !"none".equals(accessToken)) {
            blacklistAccessToken(accessToken);
        }

        if (refreshToken != null && !"none".equals(refreshToken)) {
            removeRefreshToken(refreshToken);
        }
    }

    /**
     * Blacklist access token until its expiration
     */
    private void blacklistAccessToken(String accessToken) {
        try {
            Jwt decoded = jwtService.decode(accessToken);
            String jti = decoded.getId();
            Instant exp = decoded.getExpiresAt();

            if (jti != null && exp != null && exp.isAfter(Instant.now())) {
                Duration ttl = Duration.between(Instant.now(), exp);
                redisTemplate.opsForValue().set("at_revoked:" + jti, "1", ttl);
            }
        } catch (Exception ignored) {
            // Ignore invalid token silently
        }
    }

    @Override
    public void logoutAll(String accessToken) {
        if (accessToken == null || "none".equals(accessToken)) {
            throw new ResourceInvalidException("Please sign in first");
        }

        UUID userId = extractUserId(accessToken);
        if (userId == null) {
            throw new ResourceInvalidException("Invalid access token");
        }

        blacklistAccessToken(accessToken);

        // Delete all user refresh tokens
        String pattern = "rt_user:" + userId + ":*";
        redisTemplate.keys(pattern).forEach(redisTemplate::delete);
    }

    @Override
    public void forgotPassword(String email) {
        if (!userService.existsByEmail(email)) {
            throw new ResourceNotFoundException("Email " + email + " not found");
        }
        String otp = otpCodeService.generateOTP(email);
        mailService.sendForgotPasswordEmail(email, otp, "forgotPassword");
    }

    @Transactional
    @Override
    public OtpVerificationResponse verifyOtp(String email, String inputOtp) {
        boolean validOTP = otpCodeService.validateCode("OTP:" + email, inputOtp);
        if (!validOTP) {
            throw new ResourceInvalidException("OTP is not valid or expired");
        }
        otpCodeService.deleteCode("OTP:" + email);
        String identifyCode = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set("RESET:" + email, identifyCode, 3, TimeUnit.MINUTES);
        return new OtpVerificationResponse(identifyCode);
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        boolean isValidCode = otpCodeService.validateCode("RESET:" + request.email(), request.identifyCode());
        if (!isValidCode) {
            throw new ResourceInvalidException("Identify code is not valid");
        }
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new ResourceInvalidException("Confirm passwords do not match.");
        }
        otpCodeService.deleteCode("RESET:" + request.email());
        userService.resetPassword(request.email(), passwordEncoder.encode(request.newPassword()));
    }


    /**
     * Store refresh token in Redis with TTL until token expiration.
     */
    private void storeRefreshToken(UUID userId, String refreshToken) {
        Jwt decoded = jwtService.decode(refreshToken);
        String jti = decoded.getId();
        Instant exp = decoded.getExpiresAt();
        if (jti != null && exp != null) {
            Duration ttl = Duration.between(Instant.now(), exp);
            redisTemplate.opsForValue().set(buildRtKey(userId, jti), "1", ttl);
        }
    }

    /**
     * Build Redis key for refresh token entry.
     */
    private String buildRtKey(UUID userId, String jti) {
        return "rt_user:" + userId + ":" + jti;
    }

    /**
     * Ensure user account is active before login.
     */
    private void checkAccountActive(User currentUser) {
        if (!currentUser.isEmailVerified()) {
            throw new AccessDeniedException("Email is not verified");
        }
        if (!currentUser.isActive()) {
            throw new AccessDeniedException("User account is locked");
        }
    }

    /**
     * Ensure user has a password configured before login.
     */
    private void checkPasswordExists(User user) {
        if (user.getPasswordHash() == null || user.getPasswordHash().isEmpty()) {
            throw new BadCredentialsException("Your account has no password.");
        }
    }

    /**
     * Authenticate user with provided username and password.
     */
    private Authentication authenticate(String username, String password) {
        return authenticationManagerBuilder.getObject().authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
    }

    private UUID extractUserId(String token) {
        try {
            Jwt decoded = jwtService.decode(token);
            return UUID.fromString(decoded.getSubject());
        } catch (Exception e) {
            return null;
        }
    }

    private void removeRefreshToken(String refreshToken) {
        try {
            Jwt decoded = jwtService.decode(refreshToken);
            UUID uid = UUID.fromString(decoded.getSubject());
            String jti = decoded.getId();
            redisTemplate.delete(buildRtKey(uid, jti));
        } catch (Exception ignored) {

        }
    }
}
