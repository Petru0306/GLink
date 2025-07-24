package com.greenlink.greenlink.service;

import com.greenlink.greenlink.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Value("${spring.mail.username:no-reply@greenlink.com}")
    private String fromEmail;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Override
    public void sendVerificationEmail(User user, String verificationUrl) {
        // In development mode, just log the verification details instead of actually sending an email
        logger.info("============= VERIFICATION EMAIL ==============");
        logger.info("To: {}", user.getEmail());
        logger.info("Subject: Verify Your GreenLink Account");
        logger.info("Message: Hi {}, please verify your account by clicking the following link: {}", 
                    user.getFirstName() + " " + user.getLastName(), verificationUrl);
        logger.info("================================================");
    }

    @Override
    public void sendPasswordResetEmail(User user, String resetUrl) {
        // In development mode, just log the reset details instead of actually sending an email
        logger.info("============= PASSWORD RESET EMAIL ==============");
        logger.info("To: {}", user.getEmail());
        logger.info("Subject: Reset Your GreenLink Password");
        logger.info("Message: Hi {}, please reset your password by clicking the following link: {}", 
                    user.getFirstName() + " " + user.getLastName(), resetUrl);
        logger.info("================================================");
    }

    // For now, we don't need this method as we're just logging emails
    // We'll implement it properly when we need to send actual emails
}
