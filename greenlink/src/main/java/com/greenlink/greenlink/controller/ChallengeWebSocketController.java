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
        // This method can be used for general challenge action handling
        return "Action received: " + action;
    }
} 