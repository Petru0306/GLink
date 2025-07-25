package com.greenlink.greenlink.controller;

import com.greenlink.greenlink.model.PointEvent;
import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.service.PointsService;
import com.greenlink.greenlink.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/points")
public class PointsController {

    @Autowired
    private PointsService pointsService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String showPointsHistory(Model model, @AuthenticationPrincipal User currentUser,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "20") int size) {
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Get fresh user data from database to ensure level is calculated correctly
        User freshUser = userService.getUserById(currentUser.getId());
        if (freshUser == null) {
            return "redirect:/login";
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<PointEvent> pointEvents = pointsService.getUserPointHistory(freshUser.getId(), pageable);
        
        // Get user statistics
        int totalPointsEarned = pointsService.getTotalPointsEarned(freshUser.getId());
        long levelUpCount = pointsService.getLevelUpCount(freshUser.getId());
        Map<String, Long> eventTypeStats = pointsService.getEventTypeStats(freshUser.getId());
        List<PointEvent> recentEvents = pointsService.getRecentEvents(freshUser.getId(), 7); // Last 7 days
        List<PointEvent> levelUpEvents = pointsService.getLevelUpEvents(freshUser.getId());

        model.addAttribute("pointEvents", pointEvents);
        model.addAttribute("totalPointsEarned", totalPointsEarned);
        model.addAttribute("levelUpCount", levelUpCount);
        model.addAttribute("eventTypeStats", eventTypeStats);
        model.addAttribute("recentEvents", recentEvents);
        model.addAttribute("levelUpEvents", levelUpEvents);
        model.addAttribute("currentUser", freshUser);
        
        // Get user's rank in leaderboard
        int userRank = userService.getUserRank(freshUser.getId());
        model.addAttribute("userRank", "#" + userRank);

        return "points/history";
    }

    @GetMapping("/api/history")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getPointsHistoryApi(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        if (currentUser == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not authenticated"));
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<PointEvent> pointEvents = pointsService.getUserPointHistory(currentUser.getId(), pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("pointEvents", pointEvents.getContent());
        response.put("totalPages", pointEvents.getTotalPages());
        response.put("currentPage", pointEvents.getNumber());
        response.put("totalElements", pointEvents.getTotalElements());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getPointsStats(@AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not authenticated"));
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPointsEarned", pointsService.getTotalPointsEarned(currentUser.getId()));
        stats.put("levelUpCount", pointsService.getLevelUpCount(currentUser.getId()));
        stats.put("eventTypeStats", pointsService.getEventTypeStats(currentUser.getId()));
        stats.put("recentEvents", pointsService.getRecentEvents(currentUser.getId(), 7));
        stats.put("levelUpEvents", pointsService.getLevelUpEvents(currentUser.getId()));

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/api/leaderboard")
    @ResponseBody
    public ResponseEntity<List<User>> getLeaderboard(@RequestParam(defaultValue = "10") int limit) {
        List<User> topUsers = userService.getTopUsers(limit);
        return ResponseEntity.ok(topUsers);
    }

    @GetMapping("/leaderboard")
    public String showLeaderboard(Model model, @RequestParam(defaultValue = "10") int limit) {
        List<User> topUsers = userService.getTopUsers(limit);
        model.addAttribute("topUsers", topUsers);
        model.addAttribute("limit", limit);
        return "points/leaderboard";
    }
} 