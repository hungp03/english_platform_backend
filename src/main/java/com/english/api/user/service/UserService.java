package com.english.api.user.service;

import com.english.api.user.model.User;

import java.util.UUID;

/**
 * Created by hungpham on 9/23/2025
 */
public interface UserService {
    User findByEmail (String email);
    User findById(UUID uuid);
}
