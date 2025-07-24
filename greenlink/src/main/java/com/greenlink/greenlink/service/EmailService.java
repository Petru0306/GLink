package com.greenlink.greenlink.service;

import com.greenlink.greenlink.model.User;

public interface EmailService {
    void sendVerificationEmail(User user, String verificationUrl);
    void sendPasswordResetEmail(User user, String resetUrl);
}
