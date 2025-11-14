package com.english.api.auth.service.impl;

import com.english.api.auth.dto.request.AuthRequest;
import com.english.api.auth.dto.request.LinkGoogleAccountRequest;
import com.english.api.auth.dto.request.RegisterRequest;
import com.english.api.auth.dto.request.ResetPasswordRequest;
import com.english.api.auth.dto.response.AuthResponse;
import com.english.api.auth.dto.response.LinkAccountResponse;
import com.english.api.auth.dto.response.OtpVerificationResponse;
import com.english.api.auth.dto.response.UserLoginResponse;
import com.english.api.auth.model.VerificationToken;
import com.english.api.auth.security.CustomUserDetails;
import com.english.api.auth.service.AuthService;
import com.english.api.auth.service.JwtService;
import com.english.api.auth.service.OTPCodeService;
import com.english.api.auth.service.VerificationTokenService;
import com.english.api.auth.util.SecurityUtil;
import com.english.api.common.exception.AccessDeniedException;
import com.english.api.common.exception.ResourceAlreadyExistsException;
import com.english.api.common.exception.ResourceInvalidException;
import com.english.api.common.exception.ResourceNotFoundException;
import com.english.api.mail.service.MailService;
import com.english.api.user.model.Role;
import com.english.api.user.model.User;
import com.english.api.user.repository.RoleRepository;
import com.english.api.user.service.UserService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
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
    private final com.english.api.user.repository.UserOAuth2TokenRepository oauth2TokenRepository;
    private final org.springframework.web.client.RestTemplate restTemplate;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    @Value("${app.client-url}")
    private String clientUrl;

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
                .providerUid(null)
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

    @Transactional
    @Override
    public LinkAccountResponse linkGoogleAccount(LinkGoogleAccountRequest request) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        User currentUser = userService.findById(currentUserId);

        if (request.idToken() == null && request.authorizationCode() == null) {
            throw new ResourceInvalidException("Either idToken or authorizationCode must be provided");
        }

        try {
            String googleUid;
            String email;
            String accessToken = null;
            String refreshToken = null;
            Long expiresIn = null;
            
            // If authorization code is provided, exchange it for tokens
            if (request.authorizationCode() != null && !request.authorizationCode().isEmpty()) {
                String redirectUri = request.redirectUri() != null ? request.redirectUri() : (clientUrl + "/auth/callback");
                GoogleTokenResponse tokenResponse = exchangeAuthorizationCode(request.authorizationCode(), redirectUri);
                accessToken = tokenResponse.accessToken;
                refreshToken = tokenResponse.refreshToken;
                expiresIn = tokenResponse.expiresIn;
                
                // Verify ID token from response
                GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                        new NetHttpTransport(),
                        new GsonFactory()
                )
                .setAudience(Collections.singletonList(googleClientId))
                .build();

                GoogleIdToken idToken = verifier.verify(tokenResponse.idToken);
                if (idToken == null) {
                    throw new ResourceInvalidException("Invalid Google ID token from authorization code");
                }

                GoogleIdToken.Payload payload = idToken.getPayload();
                googleUid = payload.getSubject();
                email = payload.getEmail();
            } 
            // Otherwise use the provided ID token
            else if (request.idToken().startsWith("eyJ")) {
                // It's a JWT ID token
                GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                        new NetHttpTransport(),
                        new GsonFactory()
                )
                .setAudience(Collections.singletonList(googleClientId))
                .build();

                GoogleIdToken idToken = verifier.verify(request.idToken());
                if (idToken == null) {
                    throw new ResourceInvalidException("Invalid Google ID token");
                }

                GoogleIdToken.Payload payload = idToken.getPayload();
                googleUid = payload.getSubject();
                email = payload.getEmail();
            } else {
                // It's an access token, verify via userinfo endpoint
                String userInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo";
                java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
                java.net.http.HttpRequest httpRequest = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create(userInfoUrl))
                        .header("Authorization", "Bearer " + request.idToken())
                        .GET()
                        .build();

                java.net.http.HttpResponse<String> response = client.send(httpRequest, 
                        java.net.http.HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() != 200) {
                    throw new ResourceInvalidException("Invalid Google access token");
                }

                com.google.gson.JsonObject userInfo = com.google.gson.JsonParser.parseString(response.body()).getAsJsonObject();
                googleUid = userInfo.get("sub").getAsString();
                email = userInfo.has("email") ? userInfo.get("email").getAsString() : null;
            }

            if (email == null || googleUid == null) {
                throw new ResourceInvalidException("Invalid Google token: missing email or user ID");
            }

            Optional<User> existingGoogleUser = userService.findByProviderAndProviderUid("GOOGLE", googleUid);
            if (existingGoogleUser.isPresent() && !existingGoogleUser.get().getId().equals(currentUserId)) {
                throw new ResourceAlreadyExistsException("This Google account is already linked to another user");
            }

            if (currentUser.getProviderUid() != null && !currentUser.getProviderUid().isEmpty()) {
                throw new ResourceInvalidException("This account is already linked to a provider account");
            }

            currentUser.setProvider("GOOGLE");
            currentUser.setProviderUid(googleUid);
            currentUser.setEmailVerified(true);
            userService.save(currentUser);

            // Store OAuth2 tokens for calendar integration if we obtained them
            if (accessToken != null && !accessToken.isEmpty()) {
                storeOAuth2TokensFromExchange(currentUser, accessToken, refreshToken, expiresIn);
            }

            return new LinkAccountResponse(
                    "Google account linked successfully",
                    "GOOGLE",
                    email
            );

        } catch (GeneralSecurityException | IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new ResourceInvalidException("Invalid Google token: " + e.getMessage());
        }
    }

    private GoogleTokenResponse exchangeAuthorizationCode(String authorizationCode, String redirectUri) {
        String tokenEndpoint = "https://oauth2.googleapis.com/token";
        
        org.springframework.util.LinkedMultiValueMap<String, String> params = new org.springframework.util.LinkedMultiValueMap<>();
        params.add("code", authorizationCode);
        params.add("client_id", googleClientId);
        params.add("client_secret", googleClientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);

        org.springframework.http.HttpEntity<org.springframework.util.MultiValueMap<String, String>> request = 
                new org.springframework.http.HttpEntity<>(params, headers);

        try {
            org.springframework.http.ResponseEntity<java.util.Map> response = restTemplate.postForEntity(
                    tokenEndpoint, 
                    request, 
                    java.util.Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                java.util.Map<String, Object> body = response.getBody();
                return new GoogleTokenResponse(
                        (String) body.get("access_token"),
                        (String) body.get("refresh_token"),
                        (String) body.get("id_token"),
                        body.get("expires_in") != null ? ((Number) body.get("expires_in")).longValue() : null
                );
            } else {
                throw new ResourceInvalidException("Failed to exchange authorization code for tokens");
            }
        } catch (Exception e) {
            throw new ResourceInvalidException("Failed to exchange authorization code: " + e.getMessage());
        }
    }

    private static class GoogleTokenResponse {
        String accessToken;
        String refreshToken;
        String idToken;
        Long expiresIn;

        GoogleTokenResponse(String accessToken, String refreshToken, String idToken, Long expiresIn) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.idToken = idToken;
            this.expiresIn = expiresIn;
        }
    }

    private void storeOAuth2TokensFromExchange(User user, String accessToken, String refreshToken, Long expiresIn) {
        try {
            com.english.api.user.model.UserOAuth2Token token = oauth2TokenRepository
                    .findByUserIdAndProvider(user.getId(), "GOOGLE")
                    .orElse(com.english.api.user.model.UserOAuth2Token.builder()
                            .user(user)
                            .provider("GOOGLE")
                            .build());

            token.setAccessToken(accessToken);
            
            if (refreshToken != null && !refreshToken.isEmpty()) {
                token.setRefreshToken(refreshToken);
            }
            
            if (expiresIn != null) {
                java.time.OffsetDateTime expiresAt = java.time.OffsetDateTime.now()
                        .plusSeconds(expiresIn);
                token.setTokenExpiresAt(expiresAt);
            }

            oauth2TokenRepository.save(token);
        } catch (Exception e) {
            // Log but don't fail the linking process
            System.err.println("Error storing OAuth2 tokens for user " + user.getId() + ": " + e.getMessage());
        }
    }

    @Transactional
    @Override
    public LinkAccountResponse unlinkGoogleAccount() {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        User currentUser = userService.findById(currentUserId);

        if (!"GOOGLE".equalsIgnoreCase(currentUser.getProvider())) {
            throw new ResourceInvalidException("This account is not linked to a Google account");
        }

        if (currentUser.getPasswordHash() == null || currentUser.getPasswordHash().isEmpty()) {
            throw new ResourceInvalidException("Cannot unlink Google account: No password set. Please set a password first.");
        }

        String previousEmail = currentUser.getEmail();
        currentUser.setProvider("local");
        currentUser.setProviderUid(null);
        userService.save(currentUser);

        // Delete OAuth2 tokens when unlinking
        oauth2TokenRepository.findByUserIdAndProvider(currentUserId, "GOOGLE")
                .ifPresent(oauth2TokenRepository::delete);

        return new LinkAccountResponse(
                "Google account unlinked successfully",
                "local",
                previousEmail
        );
    }
}
