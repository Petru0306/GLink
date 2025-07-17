package com.greenlink.greenlink.controller;

import com.greenlink.greenlink.service.ChallengeTrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ChallengeWebSocketController {

    @Autowired
    private ChallengeTrackingService challengeTrackingService;

    @MessageMapping("/challenge-action")
    @SendTo("/topic/challenge-updates")
    public String handleChallengeAction(String action) {
        // Extract userId from authentication context - this is a placeholder
        // In a real implementation, you would get this from the security context
        Long userId = 1L;
        
        // Track the challenge action
        challengeTrackingService.trackUserAction(userId, action, null);
        
        return "Action received and tracked: " + action;
    }
} 