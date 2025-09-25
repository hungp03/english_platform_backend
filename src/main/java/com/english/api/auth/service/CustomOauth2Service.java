package com.english.api.auth.service;

import com.english.api.user.model.User;

/**
 * Created by hungpham on 9/25/2025
 */
public interface CustomOauth2Service {
    User processOAuth2User(String email, String name, String socialId, String provider);
}
