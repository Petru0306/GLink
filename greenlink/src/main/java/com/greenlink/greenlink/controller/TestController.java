package com.greenlink.greenlink.controller;

import com.greenlink.greenlink.service.ChallengeTrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private ChallengeTrackingService challengeTrackingService;

    @GetMapping("/test-offer-challenge")
    public String testOfferChallenge(@RequestParam Long userId) {
        try {
            challengeTrackingService.trackUserAction(userId, "MARKETPLACE_OFFER_MADE", null);
            return "Offer challenge tracking triggered for user: " + userId;
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
} 