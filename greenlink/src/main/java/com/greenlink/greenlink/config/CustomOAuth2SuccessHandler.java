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
        
        try {
            if (authentication.getPrincipal() instanceof OAuth2User) {
                OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                
                // Determine the provider from the request
                String provider = determineProvider(request, authentication);
                logger.info("Processing OAuth2 user from provider: {}", provider);
                
                // Process the OAuth2 user
                User user = oauth2Service.processOAuth2User(oauth2User, provider);
                logger.info("OAuth2 user processed successfully: {}", user.getEmail());
            } else {
                logger.warn("Authentication principal is not OAuth2User: {}", authentication.getPrincipal().getClass());
            }
        } catch (Exception e) {
            logger.error("Error processing OAuth2 user", e);
            // Don't throw the exception, just log it and continue with the redirect
        }
        
        // Check if this is a popup window request
        String referer = request.getHeader("Referer");
        boolean isPopup = request.getParameter("popup") != null || 
                          (referer != null && referer.contains("popup=true"));
        
        if (isPopup) {
            // Handle popup window flow - send a message to the parent window
            String redirectUrl = determineTargetUrl(request, response, authentication);
            response.setContentType("text/html");
            String htmlResponse = "<!DOCTYPE html>\n" +
                                 "<html>\n" +
                                 "<head><title>Login Successful</title></head>\n" +
                                 "<body>\n" +
                                 "<script>\n" +
                                 "  window.opener.postMessage({type: 'oauth2-success', redirectUrl: '" + redirectUrl + "'}, window.location.origin);\n" +
                                 "  window.close();\n" +
                                 "</script>\n" +
                                 "<p>Login successful. You can close this window.</p>\n" +
                                 "</body>\n" +
                                 "</html>";
            response.getWriter().write(htmlResponse);
            response.flushBuffer();
        } else {
            // Normal redirect flow
            super.onAuthenticationSuccess(request, response, authentication);
        }
    }
    
    private String determineProvider(HttpServletRequest request, Authentication authentication) {
        // Try to get from request URI first
        String requestURI = request.getRequestURI();
        logger.info("Request URI: {}", requestURI);
        
        if (requestURI.contains("/oauth2/authorization/google")) {
            return "google";
        } else if (requestURI.contains("/oauth2/authorization/github")) {
            return "github";
        }
        
        // Try to get from request parameters
        String clientRegistrationId = request.getParameter("registrationId");
        if (clientRegistrationId != null) {
            logger.info("Found registrationId in parameters: {}", clientRegistrationId);
            return clientRegistrationId;
        }
        
        // Try to get from authentication name
        String authName = authentication.getName();
        logger.info("Authentication name: {}", authName);
        
        // Check if the name contains provider info
        if (authName.contains("github")) {
            return "github";
        } else if (authName.contains("google")) {
            return "google";
        }
        
        // Last resort: check the OAuth2User attributes
        if (authentication.getPrincipal() instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
            Object id = oauth2User.getAttribute("id");
            Object sub = oauth2User.getAttribute("sub");
            
            logger.info("OAuth2User attributes - id: {}, sub: {}", id, sub);
            
            // GitHub uses numeric ID, Google uses "sub"
            if (id != null && sub == null) {
                return "github";
            } else if (sub != null) {
                return "google";
            }
        }
        
        logger.warn("Could not determine OAuth2 provider from request URI: {}", requestURI);
        return "unknown";
    }
} 