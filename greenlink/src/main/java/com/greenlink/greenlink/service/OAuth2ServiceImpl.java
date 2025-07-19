package com.greenlink.greenlink.service;

import com.greenlink.greenlink.model.Role;
import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class OAuth2ServiceImpl implements OAuth2Service {
    
    private static final Logger logger = LoggerFactory.getLogger(OAuth2ServiceImpl.class);
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public OAuth2ServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    public User processOAuth2User(OAuth2User oauth2User, String provider) {
        logger.info("Processing OAuth2 user from provider: {}", provider);
        
        String email = extractEmail(oauth2User, provider);
        String providerId = extractProviderId(oauth2User, provider);
        String name = extractName(oauth2User, provider);
        String picture = extractPicture(oauth2User, provider);
        
        logger.info("Extracted user info - Email: {}, Provider ID: {}, Name: {}", email, providerId, name);
        
        // Check if user already exists with this OAuth2 provider
        User existingUser = userRepository.findByOauth2ProviderAndOauth2ProviderId(provider, providerId).orElse(null);
        
        if (existingUser != null) {
            logger.info("Found existing OAuth2 user: {}", existingUser.getEmail());
            return existingUser;
        }
        
        // Check if user exists with this email (from regular registration)
        existingUser = userRepository.findByEmail(email).orElse(null);
        
        if (existingUser != null) {
            logger.info("Found existing user with email: {}, linking OAuth2 account", email);
            // Link OAuth2 account to existing user
            existingUser.setOauth2Provider(provider);
            existingUser.setOauth2ProviderId(providerId);
            existingUser.setOauth2Name(name);
            existingUser.setOauth2Picture(picture);
            return userRepository.save(existingUser);
        }
        
        // Create new user
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setFirstName(extractFirstName(name));
        newUser.setLastName(extractLastName(name));
        newUser.setPassword(passwordEncoder.encode("oauth2_user_" + System.currentTimeMillis())); // Random password
        newUser.setRole(Role.USER);
        newUser.setPoints(0);
        newUser.setLevel(1);
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setOauth2Provider(provider);
        newUser.setOauth2ProviderId(providerId);
        newUser.setOauth2Name(name);
        newUser.setOauth2Picture(picture);
        
        logger.info("Creating new OAuth2 user: {}", email);
        return userRepository.save(newUser);
    }
    
    @Override
    public User findByOAuth2ProviderId(String provider, String providerId) {
        return userRepository.findByOauth2ProviderAndOauth2ProviderId(provider, providerId).orElse(null);
    }
    
    @Override
    public boolean isOAuth2User(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        return user != null && user.getOauth2Provider() != null;
    }
    
    private String extractEmail(OAuth2User oauth2User, String provider) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        
        switch (provider.toLowerCase()) {
            case "google":
                return (String) attributes.get("email");
            case "github":
                return (String) attributes.get("email");
            default:
                logger.warn("Unknown OAuth2 provider: {}", provider);
                return null;
        }
    }
    
    private String extractProviderId(OAuth2User oauth2User, String provider) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        
        switch (provider.toLowerCase()) {
            case "google":
                return (String) attributes.get("sub");
            case "github":
                return String.valueOf(attributes.get("id"));
            default:
                logger.warn("Unknown OAuth2 provider: {}", provider);
                return null;
        }
    }
    
    private String extractName(OAuth2User oauth2User, String provider) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        
        switch (provider.toLowerCase()) {
            case "google":
                return (String) attributes.get("name");
            case "github":
                return (String) attributes.get("login"); // GitHub username
            default:
                logger.warn("Unknown OAuth2 provider: {}", provider);
                return null;
        }
    }
    
    private String extractPicture(OAuth2User oauth2User, String provider) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        
        switch (provider.toLowerCase()) {
            case "google":
                return (String) attributes.get("picture");
            case "github":
                return (String) attributes.get("avatar_url");
            default:
                logger.warn("Unknown OAuth2 provider: {}", provider);
                return null;
        }
    }
    
    private String extractFirstName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "OAuth2";
        }
        String[] parts = fullName.trim().split("\\s+");
        return parts[0];
    }
    
    private String extractLastName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "User";
        }
        String[] parts = fullName.trim().split("\\s+");
        return parts.length > 1 ? parts[1] : "";
    }
} 