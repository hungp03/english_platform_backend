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
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;


@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    @Value("${app.client-url}")
    private String client;

    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    private final String[] whiteList = {
            "/",
            "/api/auth/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/login/oauth2/**",
            "/oauth2/**",
            "/login/oauth2/callback/**",
    };

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


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationConverter jwtAuthenticationConverter,
                                                   JwtBlacklistFilter jwtBlacklistFilter,
                                                   AccountLockFilter accountLockFilter) throws Exception {
        http.cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                // Add custom filter after default authentication
                .addFilterAfter(jwtBlacklistFilter, BearerTokenAuthenticationFilter.class)
                .addFilterAfter(accountLockFilter, BearerTokenAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(whiteList).permitAll()
                        .requestMatchers("/api/hello").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .oauth2Login(oauth -> oauth
                        .successHandler(oAuth2LoginSuccessHandler)
                        .failureHandler((request, response, exception) -> {
                            System.err.println("OAuth2 Login Error: " + exception.getMessage());
                            System.err.println("Request URI: " + request.getRequestURI());
                            System.err.println("Query String: " + request.getQueryString());
                            response.sendRedirect(client + "/authentication/error?isLogin=false");
                        })
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .bearerTokenResolver(new CookieBearerTokenResolver("access_token", List.of(whiteList)))
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                        .authenticationEntryPoint(authenticationEntryPoint)
                );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
