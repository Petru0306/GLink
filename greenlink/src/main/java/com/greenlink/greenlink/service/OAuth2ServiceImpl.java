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
        
        try {
            String email = extractEmail(oauth2User, provider);
            String providerId = extractProviderId(oauth2User, provider);
            String name = extractName(oauth2User, provider);
            String picture = extractPicture(oauth2User, provider);
            
            logger.info("Extracted user info - Email: {}, Provider ID: {}, Name: {}, Picture: {}", 
                       email, providerId, name, picture != null ? "present" : "null");
            
            if (email == null || providerId == null) {
                logger.error("Critical OAuth2 data missing - Email: {}, Provider ID: {}", email, providerId);
                throw new IllegalArgumentException("Email or Provider ID is null");
            }
            
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
                User savedUser = userRepository.save(existingUser);
                logger.info("Successfully linked OAuth2 account to existing user: {}", savedUser.getEmail());
                return savedUser;
            }
            
            // Create new user
            User newUser = new User();
            newUser.setEmail(email);
            
            // Use username from email if name is not available
            if (name == null || name.trim().isEmpty()) {
                String usernameFromEmail = extractUsernameFromEmail(email);
                if (usernameFromEmail != null && !usernameFromEmail.isEmpty()) {
                    name = usernameFromEmail;
                    logger.info("Using username from email as name: {}", name);
                }
            }
            
            newUser.setFirstName(extractFirstName(name));
            newUser.setLastName(extractLastName(name));
            // Generate a strong random password that meets validation requirements
            String randomPassword = generateSecureRandomPassword();
            newUser.setPassword(passwordEncoder.encode(randomPassword));
            newUser.setRole(Role.USER);
            newUser.setPoints(0);
            newUser.setLevel(1);
            newUser.setCreatedAt(LocalDateTime.now());
            newUser.setOauth2Provider(provider);
            newUser.setOauth2ProviderId(providerId);
            newUser.setOauth2Name(name);
            newUser.setOauth2Picture(picture);
            
            logger.info("Creating new OAuth2 user: {}", email);
            User savedUser = userRepository.save(newUser);
            logger.info("Successfully created new OAuth2 user: {}", savedUser.getEmail());
            return savedUser;
            
        } catch (Exception e) {
            logger.error("Error processing OAuth2 user from provider: {}", provider, e);
            throw e;
        }
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
        logger.info("Extracting email for provider: {}, available attributes: {}", provider, attributes.keySet());
        
        switch (provider.toLowerCase()) {
            case "google":
                return (String) attributes.get("email");
            case "github":
                // GitHub might not always provide email in the basic scope
                String email = (String) attributes.get("email");
                if (email == null) {
                    // Try alternative field names
                    email = (String) attributes.get("primary_email");
                    if (email == null) {
                        // Generate a placeholder email if none is provided
                        String login = (String) attributes.get("login");
                        if (login != null) {
                            email = login + "@github.user";
                            // We'll set the name in the parent method using this login ID 
                            logger.warn("No email found for GitHub user {}, using placeholder: {}", login, email);
                        }
                    }
                }
                return email;
            default:
                logger.warn("Unknown OAuth2 provider: {}", provider);
                return (String) attributes.get("email");
        }
    }
    
    private String extractProviderId(OAuth2User oauth2User, String provider) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        logger.info("Extracting provider ID for provider: {}", provider);
        
        switch (provider.toLowerCase()) {
            case "google":
                return (String) attributes.get("sub");
            case "github":
                Object id = attributes.get("id");
                if (id != null) {
                    return String.valueOf(id);
                }
                logger.error("GitHub ID not found in attributes: {}", attributes.keySet());
                return null;
            default:
                logger.warn("Unknown OAuth2 provider: {}", provider);
                return null;
        }
    }
    
    private String extractName(OAuth2User oauth2User, String provider) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        logger.info("Extracting name for provider: {}", provider);
        
        switch (provider.toLowerCase()) {
            case "google":
                return (String) attributes.get("name");
            case "github":
                // GitHub provides login (username) and name (full name)
                String name = (String) attributes.get("name");
                if (name == null || name.trim().isEmpty()) {
                    name = (String) attributes.get("login"); // Fallback to username
                }
                return name;
            default:
                logger.warn("Unknown OAuth2 provider: {}", provider);
                return (String) attributes.get("name");
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
                return (String) attributes.get("picture");
        }
    }
    
    private String extractFirstName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "OAuth2";
        }
        String[] parts = fullName.trim().split("\\s+");
        return parts[0];
    }
    
    /**
     * Extract username from email address (part before @)
     */
    private String extractUsernameFromEmail(String email) {
        if (email == null || !email.contains("@")) {
            return null;
        }
        return email.substring(0, email.indexOf('@'));
    }
    
    private String extractLastName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "User";
        }
        String[] parts = fullName.trim().split("\\s+");
        return parts.length > 1 ? parts[1] : "";
    }
    
    /**
     * Generate a secure random password that meets our password requirements
     * Requirements: at least one number, lowercase letter, uppercase letter, and special character
     */
    private String generateSecureRandomPassword() {
        String uppercaseChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowercaseChars = "abcdefghijklmnopqrstuvwxyz";
        String numberChars = "0123456789";
        String specialChars = "@#$%^&+=!.,";
        
        StringBuilder password = new StringBuilder();
        
        // Ensure at least one of each required character type
        password.append(uppercaseChars.charAt((int) (Math.random() * uppercaseChars.length())));
        password.append(lowercaseChars.charAt((int) (Math.random() * lowercaseChars.length())));
        password.append(numberChars.charAt((int) (Math.random() * numberChars.length())));
        password.append(specialChars.charAt((int) (Math.random() * specialChars.length())));
        
        // Add more random characters to reach minimum length
        String allChars = uppercaseChars + lowercaseChars + numberChars + specialChars;
        while (password.length() < 16) { // Create a 16-character password
            password.append(allChars.charAt((int) (Math.random() * allChars.length())));
        }
        
        // Shuffle the password characters
        char[] passwordArray = password.toString().toCharArray();
        for (int i = 0; i < passwordArray.length; i++) {
            int j = (int) (Math.random() * passwordArray.length);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }
        
        return new String(passwordArray);
    }
} 