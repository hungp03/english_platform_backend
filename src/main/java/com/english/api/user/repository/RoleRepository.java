package com.english.api.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.english.api.user.model.Role;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByCode(String code);
}

