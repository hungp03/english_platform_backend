package com.english.api.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import com.english.api.user.model.Role;
import com.english.api.user.model.User;
import com.english.api.user.repository.RoleRepository;
import com.english.api.user.repository.UserRepository;
import com.github.f4b6a3.uuid.UuidCreator;

@Component
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        initializeRoles();
        initializeUsers();
    }

    private void initializeRoles() {
        if (roleRepository.count() == 0) {
            Role adminRole = Role.builder()
                    .id(UuidCreator.getTimeOrderedEpoch())
                    .code("ADMIN")
                    .name("Administrator")
                    .build();

            Role userRole = Role.builder()
                    .id(UuidCreator.getTimeOrderedEpoch())
                    .code("USER")
                    .name("Normal User")
                    .build();

            roleRepository.saveAll(List.of(adminRole, userRole));
            System.out.println("-----INIT ROLES-----");
        } else {
            System.out.println("-----ROLES ALREADY EXIST, SKIP INIT ROLES-----");
        }
    }

    private void initializeUsers() {
        if (userRepository.count() == 0) {
            Role adminRole = roleRepository.findByCode("ADMIN")
                    .orElseThrow(() -> new IllegalStateException("ADMIN role not found"));
            Role userRole = roleRepository.findByCode("USER")
                    .orElseThrow(() -> new IllegalStateException("USER role not found"));

            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);

            User adminUser = User.builder()
                    .id(UuidCreator.getTimeOrderedEpoch())
                    .email("admin@gmail.com")
                    .passwordHash(encoder.encode("123456"))
                    .fullName("Admin User")
                    .provider("local")
                    .providerUid("admin@gmail.com")
                    .isActive(true)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .roles(Set.of(adminRole, userRole)) // ADMIN & USER
                    .build();

            User normalUser = User.builder()
                    .id(UuidCreator.getTimeOrderedEpoch())
                    .email("user@gmail.com")
                    .passwordHash(encoder.encode("123456"))
                    .fullName("Normal User")
                    .provider("local")
                    .providerUid("user@gmail.com")
                    .isActive(true)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .roles(Set.of(userRole)) 
                    .build();

            userRepository.saveAll(List.of(adminUser, normalUser));

            System.out.println("-----INIT ADMIN & USER ACCOUNTS-----");
        } else {
            System.out.println("-----USERS ALREADY EXIST, SKIP INIT USERS-----");
        }
    }
}

