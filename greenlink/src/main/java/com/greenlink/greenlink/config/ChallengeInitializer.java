package com.greenlink.greenlink.config;

import com.greenlink.greenlink.service.ChallengeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ChallengeInitializer implements CommandLineRunner {

    @Autowired
    private ChallengeService challengeService;

    @Override
    public void run(String... args) throws Exception {
        // Ensure default challenges are created
        challengeService.createDefaultChallenges();
        System.out.println("✅ Default challenges initialized successfully");
    }
} 