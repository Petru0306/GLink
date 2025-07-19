package com.greenlink.greenlink.service;

import com.greenlink.greenlink.model.User;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface OAuth2Service {
    User processOAuth2User(OAuth2User oauth2User, String provider);
    User findByOAuth2ProviderId(String provider, String providerId);
    boolean isOAuth2User(String email);
} 