package com.greenlink.greenlink.config;

import com.greenlink.greenlink.service.OAuth2Service;
import com.greenlink.greenlink.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2SuccessHandler.class);
    
    private final OAuth2Service oauth2Service;
    
    public CustomOAuth2SuccessHandler(OAuth2Service oauth2Service) {
        this.oauth2Service = oauth2Service;
        setDefaultTargetUrl("/dashboard");
    }
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                      HttpServletResponse response, 
                                      Authentication authentication) throws IOException, ServletException {
        
        logger.info("OAuth2 authentication successful");
        
        if (authentication.getPrincipal() instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            
            // Determine the provider from the request
            String provider = determineProvider(request);
            logger.info("Processing OAuth2 user from provider: {}", provider);
            
            // Process the OAuth2 user
            User user = oauth2Service.processOAuth2User(oauth2User, provider);
            logger.info("OAuth2 user processed successfully: {}", user.getEmail());
        }
        
        super.onAuthenticationSuccess(request, response, authentication);
    }
    
    private String determineProvider(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        if (requestURI.contains("/oauth2/authorization/google")) {
            return "google";
        }
        return "unknown";
    }
} 