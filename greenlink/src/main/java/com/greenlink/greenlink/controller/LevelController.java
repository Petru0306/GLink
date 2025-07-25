package com.greenlink.greenlink.controller;

import com.greenlink.greenlink.model.User;
import com.greenlink.greenlink.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/levels")
public class LevelController {

    @Autowired
    private UserService userService;

    @GetMapping
    public String showLevels(Model model, @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Get fresh user data
        User freshUser = userService.getUserById(currentUser.getId());
        if (freshUser == null) {
            return "redirect:/login";
        }

        // Create level data
        List<Map<String, Object>> allLevels = createLevelData();
        
        // Get user's rank
        int userRank = userService.getUserRank(freshUser.getId());

        model.addAttribute("currentUser", freshUser);
        model.addAttribute("allLevels", allLevels);
        model.addAttribute("userRank", "#" + userRank);

        return "levels";
    }

    private List<Map<String, Object>> createLevelData() {
        List<Map<String, Object>> levels = new ArrayList<>();
        
        // Level data with points requirements
        int[] pointsRequired = {50, 150, 300, 500, 750, 1050, 1400, 1800, 2250, 2750, 3300, 3900, 4550, 5250, 6000};
        String[] levelNames = {
            "level.bottle.beginner", "level.trash.tamer", "level.compost.captain", "level.solar.scout", "level.green.goblin",
            "level.plastic.paladin", "level.worm.wrangler", "level.recycle.ranger", "level.eco.enchanter", "level.carbon.crusher",
            "level.led.legend", "level.sustainability.sorcerer", "level.reusable.rebel", "level.veggie.viking", "level.planet.protector"
        };

        for (int i = 0; i < 15; i++) {
            Map<String, Object> level = new HashMap<>();
            level.put("level", i + 1);
            level.put("name", levelNames[i]);
            level.put("pointsRequired", pointsRequired[i]);
            levels.add(level);
        }

        return levels;
    }
} 