package com.greenlink.greenlink.controller;

import com.greenlink.greenlink.service.LevelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("/levels")
public class LevelController {
    
    private final LevelService levelService;
    
    @Autowired
    public LevelController(LevelService levelService) {
        this.levelService = levelService;
    }
    
    @GetMapping
    public String showLevels(Model model) {
        Map<Integer, String> levelNames = levelService.getAllLevelNames();
        Map<Integer, Integer> xpRequirements = levelService.getAllXPRequirements();
        
        model.addAttribute("levelNames", levelNames);
        model.addAttribute("xpRequirements", xpRequirements);
        
        return "levels";
    }
} 