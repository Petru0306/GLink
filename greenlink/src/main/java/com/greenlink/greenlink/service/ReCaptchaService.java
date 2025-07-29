package com.greenlink.greenlink.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;

@Service
public class ReCaptchaService {
    private static final Logger logger = LoggerFactory.getLogger(ReCaptchaService.class);
    private static final String GOOGLE_RECAPTCHA_VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    @Value("${recaptcha.secret-key}")
    private String recaptchaSecretKey;

    @Value("${recaptcha.enabled:false}")
    private boolean recaptchaEnabled;

    /**
     * Verifies a reCAPTCHA response token.
     * 
     * @param token The reCAPTCHA response token
     * @param remoteIp The IP address of the user (optional)
     * @return true if verification is successful, false otherwise
     */
    public boolean validateCaptcha(String token, String remoteIp) {
        logger.debug("reCAPTCHA validation started. Enabled: {}, Token: {}, RemoteIP: {}", 
                    recaptchaEnabled, token != null ? "present" : "null", remoteIp);
        
        // If reCAPTCHA is disabled, always return true
        if (!recaptchaEnabled) {
            logger.debug("reCAPTCHA is disabled, returning true");
            return true;
        }

        // If token is missing, validation fails
        if (token == null || token.isEmpty()) {
            logger.warn("reCAPTCHA token is missing or empty");
            return false;
        }

        try {
            logger.debug("Making request to Google reCAPTCHA verification API");
            RestTemplate restTemplate = new RestTemplate();
            MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
            requestMap.add("secret", recaptchaSecretKey);
            requestMap.add("response", token);
            
            if (remoteIp != null && !remoteIp.isEmpty()) {
                requestMap.add("remoteip", remoteIp);
            }

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                GOOGLE_RECAPTCHA_VERIFY_URL, 
                org.springframework.http.HttpMethod.POST,
                new org.springframework.http.HttpEntity<>(requestMap),
                new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> body = response.getBody();
            logger.debug("Google reCAPTCHA API response: {}", body);
            
            if (body == null) {
                logger.warn("Google reCAPTCHA API returned null response body");
                return false;
            }

            boolean success = Boolean.TRUE.equals(body.get("success"));
            logger.debug("reCAPTCHA validation result: {}", success);
            
            if (!success) {
                logger.warn("reCAPTCHA validation failed. Error codes: {}", body.get("error-codes"));
            }
            
            return success;
        } catch (Exception e) {
            logger.error("Exception during reCAPTCHA validation", e);
            return false;
        }
    }
}
