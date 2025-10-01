package com.english.api.auth.service.impl;

import com.english.api.auth.service.CustomOauth2Service;
import com.english.api.common.exception.ResourceNotFoundException;
import com.english.api.user.model.Role;
import com.english.api.user.model.User;
import com.english.api.user.repository.RoleRepository;
import com.english.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

/**
 * Created by hungpham on 9/25/2025
 */
@Service
@RequiredArgsConstructor
public class CustomOauth2ServiceImpl implements CustomOauth2Service {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public User processOAuth2User(String email, String name, String socialId, String provider) {
        Optional<User> optionalUser = userRepository.findByEmailWithRoles(email);
        if (optionalUser.isPresent()) {
            return optionalUser.get();
        }

        Role userRole = roleRepository.findByCode("USER")
                .orElseThrow(() -> new ResourceNotFoundException("Role USER not found"));

        User newUser = User.builder()
                .email(email)
                .fullName(name)
                .provider(provider)
                .emailVerified(true)
                .providerUid(socialId)
                .roles(Set.of(userRole))
                .build();

        return userRepository.save(newUser);
    }
}

