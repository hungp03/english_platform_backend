package com.english.api.auth.config;

import com.english.api.auth.filter.AccountLockFilter;
import com.english.api.auth.filter.JwtBlacklistFilter;
import com.english.api.auth.security.CookieBearerTokenResolver;
import com.english.api.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Value("${app.client-url}")
    private String client;

    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    private final String[] whiteList = {
            "/",
            "/api/auth/register",
            "/api/auth/verify-register",
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/auth/logout",
            "/api/auth/logout-all",
            "/api/auth/forgot-password",
            "/api/auth/verify-otp",
            "/api/auth/reset-password",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api/media/callback"
    };

    @Bean
    public CookieBearerTokenResolver cookieBearerTokenResolver() {
        return new CookieBearerTokenResolver("access_token", Arrays.asList(whiteList));
    }

    @Bean
    public JwtBlacklistFilter jwtBlacklistFilter(RedisTemplate<String, String> redisTemplate) {
        return new JwtBlacklistFilter(redisTemplate);
    }

    @Bean
    public AccountLockFilter accountLockFilter(UserService userService, ObjectMapper objectMapper) {
        return new AccountLockFilter(userService, objectMapper);
    }

    @Bean
    public FilterRegistrationBean<JwtBlacklistFilter> disableJwtBlacklistFilter(JwtBlacklistFilter filter) {
        FilterRegistrationBean<JwtBlacklistFilter> reg = new FilterRegistrationBean<>(filter);
        reg.setEnabled(false);
        return reg;
    }

    @Bean
    public FilterRegistrationBean<AccountLockFilter> disableAccountLockFilter(AccountLockFilter filter) {
        FilterRegistrationBean<AccountLockFilter> reg = new FilterRegistrationBean<>(filter);
        reg.setEnabled(false);
        return reg;
    }

    /**
     * Chain 1: API (JWT via Cookie)
     */
    @Bean
    public SecurityFilterChain apiSecurity(HttpSecurity http,
                                           JwtAuthenticationConverter jwtAuthenticationConverter,
                                           JwtBlacklistFilter jwtBlacklistFilter,
                                           AccountLockFilter accountLockFilter,
                                           CookieBearerTokenResolver cookieBearerTokenResolver) throws Exception {

        http.securityMatcher("/api/**")
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(whiteList).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/courses/published").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/courses/slug/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/public/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/courses/*/modules/published").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/payments/stripe/webhook").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/payments/payos/webhook").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/hello").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .addFilterAfter(jwtBlacklistFilter, BearerTokenAuthenticationFilter.class)
                .addFilterAfter(accountLockFilter, BearerTokenAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint) // 401 JSON
                        .accessDeniedHandler(customAccessDeniedHandler)      // 403 JSON
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .bearerTokenResolver(cookieBearerTokenResolver)
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                );

        return http.build();
    }

    /**
     * Chain 2: OAuth2 Login (Google) – /oauth2/**
     */
    @Bean
    public SecurityFilterChain oauth2LoginSecurity(HttpSecurity http) throws Exception {
        http.securityMatcher("/oauth2/**", "/login/oauth2/**")
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .oauth2Login(oauth -> oauth
                        .loginPage("/oauth2/authorization/google")
                        .successHandler(oAuth2LoginSuccessHandler)
                                .failureHandler((request, response, exception) -> {
                            System.err.println("OAuth2 Login Error: " + exception.getMessage());
                            System.err.println("Request URI: " + request.getRequestURI());
                            System.err.println("Query String: " + request.getQueryString());
                                    response.sendRedirect(client + "/authentication/error?isLogin=false");
                                })
                );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
