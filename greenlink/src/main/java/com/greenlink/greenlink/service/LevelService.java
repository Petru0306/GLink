package com.greenlink.greenlink.service;

import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class LevelService {
    
    // Level names for levels 1-15
    private static final Map<Integer, String> LEVEL_NAMES = new HashMap<>();
    
    // XP requirements for each level (cumulative)
    private static final Map<Integer, Integer> LEVEL_XP_REQUIREMENTS = new HashMap<>();
    
    static {
        // Initialize level names
        LEVEL_NAMES.put(1, "Bottle Beginner");
        LEVEL_NAMES.put(2, "Trash Tamer");
        LEVEL_NAMES.put(3, "Compost Captain");
        LEVEL_NAMES.put(4, "Solar Scout");
        LEVEL_NAMES.put(5, "Green Goblin");
        LEVEL_NAMES.put(6, "Plastic Paladin");
        LEVEL_NAMES.put(7, "Worm Wrangler");
        LEVEL_NAMES.put(8, "Recycle Ranger");
        LEVEL_NAMES.put(9, "Eco Enchanter");
        LEVEL_NAMES.put(10, "Carbon Crusher");
        LEVEL_NAMES.put(11, "LED Legend");
        LEVEL_NAMES.put(12, "Sustainability Sorcerer");
        LEVEL_NAMES.put(13, "Reusable Rebel");
        LEVEL_NAMES.put(14, "Veggie Viking");
        LEVEL_NAMES.put(15, "Planet Protector");
        
        // Initialize XP requirements (cumulative)
        // Level 1: 0-50 XP
        // Level 2: 51-200 XP (150 more)
        // Level 3: 201-500 XP (300 more)
        // Level 4: 501-1000 XP (500 more)
        // Level 5: 1001-1750 XP (750 more)
        // Level 6: 1751-2750 XP (1000 more)
        // Level 7: 2751-4050 XP (1300 more)
        // Level 8: 4051-5700 XP (1650 more)
        // Level 9: 5701-7750 XP (2050 more)
        // Level 10: 7751-10250 XP (2500 more)
        // Level 11: 10251-13250 XP (3000 more)
        // Level 12: 13251-16750 XP (3500 more)
        // Level 13: 16751-20750 XP (4000 more)
        // Level 14: 20751-25250 XP (4500 more)
        // Level 15: 25251+ XP (5000 more)
        
        LEVEL_XP_REQUIREMENTS.put(1, 50);
        LEVEL_XP_REQUIREMENTS.put(2, 200);
        LEVEL_XP_REQUIREMENTS.put(3, 500);
        LEVEL_XP_REQUIREMENTS.put(4, 1000);
        LEVEL_XP_REQUIREMENTS.put(5, 1750);
        LEVEL_XP_REQUIREMENTS.put(6, 2750);
        LEVEL_XP_REQUIREMENTS.put(7, 4050);
        LEVEL_XP_REQUIREMENTS.put(8, 5700);
        LEVEL_XP_REQUIREMENTS.put(9, 7750);
        LEVEL_XP_REQUIREMENTS.put(10, 10250);
        LEVEL_XP_REQUIREMENTS.put(11, 13250);
        LEVEL_XP_REQUIREMENTS.put(12, 16750);
        LEVEL_XP_REQUIREMENTS.put(13, 20750);
        LEVEL_XP_REQUIREMENTS.put(14, 25250);
        LEVEL_XP_REQUIREMENTS.put(15, 30250);
    }
    
    /**
     * Calculate the user's level based on their total XP
     */
    public int calculateLevel(int totalXP) {
        if (totalXP <= 0) return 1;
        
        for (int level = 15; level >= 1; level--) {
            if (totalXP >= LEVEL_XP_REQUIREMENTS.get(level)) {
                return level;
            }
        }
        
        return 1;
    }
    
    /**
     * Get the level name for a given level
     */
    public String getLevelName(int level) {
        return LEVEL_NAMES.getOrDefault(level, "Unknown Level");
    }
    
    /**
     * Get the XP required for the next level
     */
    public int getXPForNextLevel(int currentLevel) {
        if (currentLevel >= 15) {
            return LEVEL_XP_REQUIREMENTS.get(15); // Max level reached
        }
        return LEVEL_XP_REQUIREMENTS.get(currentLevel + 1);
    }
    
    /**
     * Get the XP required for the current level
     */
    public int getXPForCurrentLevel(int currentLevel) {
        if (currentLevel <= 1) {
            return 0;
        }
        return LEVEL_XP_REQUIREMENTS.get(currentLevel - 1);
    }
    
    /**
     * Calculate progress percentage to next level
     */
    public int getProgressToNextLevel(int currentLevel, int totalXP) {
        if (currentLevel >= 15) {
            return 100; // Max level reached
        }
        
        int xpForCurrentLevel = getXPForCurrentLevel(currentLevel);
        int xpForNextLevel = getXPForNextLevel(currentLevel);
        int xpInCurrentLevel = totalXP - xpForCurrentLevel;
        int xpNeededForNextLevel = xpForNextLevel - xpForCurrentLevel;
        
        if (xpNeededForNextLevel <= 0) return 100;
        
        int progress = (xpInCurrentLevel * 100) / xpNeededForNextLevel;
        return Math.min(100, Math.max(0, progress));
    }
    
    /**
     * Get XP needed to reach next level
     */
    public int getXPNeededForNextLevel(int currentLevel, int totalXP) {
        if (currentLevel >= 15) {
            return 0; // Max level reached
        }
        
        int xpForNextLevel = getXPForNextLevel(currentLevel);
        return Math.max(0, xpForNextLevel - totalXP);
    }
    
    /**
     * Get all level names
     */
    public Map<Integer, String> getAllLevelNames() {
        return new HashMap<>(LEVEL_NAMES);
    }
    
    /**
     * Get all XP requirements
     */
    public Map<Integer, Integer> getAllXPRequirements() {
        return new HashMap<>(LEVEL_XP_REQUIREMENTS);
    }
} 