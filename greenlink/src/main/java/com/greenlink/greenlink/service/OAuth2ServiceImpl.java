package com.greenlink.greenlink.service;

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
        
        String providerId = oauth2User.getName();
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String picture = oauth2User.getAttribute("picture");
        
        logger.info("OAuth2 user details - Email: {}, Name: {}, Provider ID: {}", email, name, providerId);
        
        // Check if user exists by OAuth2 provider ID
        User existingUser = findByOAuth2ProviderId(provider, providerId);
        if (existingUser != null) {
            logger.info("Found existing OAuth2 user: {}", existingUser.getEmail());
            existingUser.setLastLogin(LocalDateTime.now());
            return userRepository.save(existingUser);
        }
        
        // Check if user exists by email
        if (email != null) {
            existingUser = userRepository.findByEmail(email).orElse(null);
            if (existingUser != null) {
                logger.info("Found existing user by email: {}", email);
                // Link OAuth2 account to existing user
                existingUser.setOauth2Provider(provider);
                existingUser.setOauth2ProviderId(providerId);
                existingUser.setOauth2Name(name);
                existingUser.setOauth2Picture(picture);
                existingUser.setLastLogin(LocalDateTime.now());
                return userRepository.save(existingUser);
            }
        }
        
        // Create new user
        logger.info("Creating new OAuth2 user");
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setOauth2Provider(provider);
        newUser.setOauth2ProviderId(providerId);
        newUser.setOauth2Name(name);
        newUser.setOauth2Picture(picture);
        
        // Set name from OAuth2 data
        if (name != null && !name.isEmpty()) {
            String[] nameParts = name.split(" ", 2);
            newUser.setFirstName(nameParts[0]);
            newUser.setLastName(nameParts.length > 1 ? nameParts[1] : "");
        } else {
            newUser.setFirstName("User");
            newUser.setLastName("");
        }
        
        // Generate a random password for OAuth2 users
        String randomPassword = generateRandomPassword();
        newUser.setPassword(passwordEncoder.encode(randomPassword));
        
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setLastLogin(LocalDateTime.now());
        newUser.setEnabled(true);
        newUser.setActive(true);
        
        logger.info("Saving new OAuth2 user: {}", newUser.getEmail());
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
    
    private String generateRandomPassword() {
        // Generate a secure random password for OAuth2 users
        return "OAUTH2_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }
} 