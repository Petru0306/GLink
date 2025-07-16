package com.greenlink.greenlink.controller;

import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.service.ChallengeTrackingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/reciclare")
public class RecyclingController {

    @Autowired
    private ChallengeTrackingService challengeTrackingService;

    @GetMapping
    public String showRecyclingMap(@AuthenticationPrincipal User user) {
        if (user != null) {
            // Track recycling map exploration for challenges
            challengeTrackingService.trackUserAction(user.getId(), "RECYCLING_MAP_EXPLORED", null);
        }
        return "reciclare";
    }
}
