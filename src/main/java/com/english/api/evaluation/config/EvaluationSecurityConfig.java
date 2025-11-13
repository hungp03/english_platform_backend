package com.english.api.evaluation.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Component;

/**
 * Permits only the evaluation callback path without session auth.
 * Signature verification still applies in the controller.
 *
 * If your app already has a SecurityFilterChain, this chain will only
 * apply to the /api/evaluation/callback/** matcher and will not affect other paths.
 */
@Component
@ConditionalOnProperty(prefix = "evaluation.webhook", name = "permitAll", havingValue = "true", matchIfMissing = false)
public class EvaluationSecurityConfig {

    @Bean
    @Order(0)
    public SecurityFilterChain evaluationCallbackChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/evaluation/callback/**")
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            )
            .csrf(csrf -> csrf.disable())
            .httpBasic(Customizer.withDefaults())
            .formLogin(form -> form.disable())
            .logout(logout -> logout.disable());
        return http.build();
    }
}
