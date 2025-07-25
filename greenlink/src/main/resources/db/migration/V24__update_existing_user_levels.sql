-- Update existing user levels based on the new leveling system
-- Level thresholds: 50, 150, 300, 500, 750, 1050, 1400, 1800, 2250, 2750, 3300, 3900, 4550, 5250, 6000

UPDATE users 
SET level = CASE 
    WHEN points < 50 THEN 1
    WHEN points < 150 THEN 2
    WHEN points < 300 THEN 3
    WHEN points < 500 THEN 4
    WHEN points < 750 THEN 5
    WHEN points < 1050 THEN 6
    WHEN points < 1400 THEN 7
    WHEN points < 1800 THEN 8
    WHEN points < 2250 THEN 9
    WHEN points < 2750 THEN 10
    WHEN points < 3300 THEN 11
    WHEN points < 3900 THEN 12
    WHEN points < 4550 THEN 13
    WHEN points < 5250 THEN 14
    WHEN points < 6000 THEN 15
    ELSE 15
END
WHERE level IS NOT NULL; 